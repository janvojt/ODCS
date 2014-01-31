package cz.cuni.mff.xrg.odcs.backend.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.db.DBHelper;
import ch.qos.logback.core.db.DriverManagerConnectionSource;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Implementation of log appender. The code is inspired by
 * {@link ch.qos.logback.core.db.DBAppenderBase}.
 *
 * The appender is designed to append into single table in Virtuoso.
 *
 * @author Petyr
 */
public class SqlAppenderImpl extends UnsynchronizedAppenderBase<ILoggingEvent>
		implements SqlAppender {

	private static final Logger LOG = LoggerFactory.getLogger(SqlAppenderImpl.class);

	/**
	 * How many logs we can commit in single query.
	 */
	private static final int LOG_BATCH_SIZE = 50;
	
	@Autowired
	private DataSource dataSource;

	/**
	 * Source of database connection.
	 */
	protected DriverManagerConnectionSource connectionSource;

	/**
	 * List into which add new logs.
	 */
	protected ArrayList<ILoggingEvent> primaryList = new ArrayList<>(100);

	/**
	 * List of logs that should be save into database.
	 */
	protected ArrayList<ILoggingEvent> secondaryList = new ArrayList<>(100);

	/**
	 * If true then the source support batch execution.
	 */
	protected boolean supportsBatchUpdates;

	/**
	 * Lock used to secure privilege access to the {@link #primaryList} and
	 * {@link #secondaryList}
	 */
	private final Object lockList = new Object();

	/**
	 * Return string that is used as insert query into logging table.
	 *
	 * @return
	 */
	private String getInsertSQL() {
		return "INSERT INTO logging"
				+ " (logLevel, timestmp, logger, message, dpu, execution, stack_trace)"
				+ " VALUES (?, ?, ? ,?, ?, ?, ?)";
	}

	/**
	 * Return not null {@link Conenction}. If failed to get connection then
	 * continue to try until success.
	 *
	 * @return
	 */
	private Connection getConnection() {
		Connection connection = null;
		while (connection == null) {
			try {
				connection = connectionSource.getConnection();
				connection.setAutoCommit(false);
			} catch (SQLException ex) {
				// wait for some time an try it again .. 
				LOG.error("Failed to get sql connection, next try in second.", ex);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException intExc) {
					// ok, continue
				}
			}
		}
		return connection;
	}

	/**
	 * Store given logs into database.
	 *
	 * @param connection
	 * @param logs
	 * @return True only if all logs has been saved into database.
	 */
	private boolean flushIntoDatabase(Connection connection, List<ILoggingEvent> logs) {
		LOG.trace("flushIntoDatabase called for {} logs", logs.size());
		try (PreparedStatement stmt = connection.prepareStatement(getInsertSQL())) {
			// append all logs
			for (ILoggingEvent item : logs) {
				bindStatement(item, stmt);
				stmt.addBatch();
			}
			// call insert
			stmt.executeBatch();
			stmt.close();
			connection.commit();
		} catch (Throwable sqle) {
			// we failed, try it again .. later
			LOG.error("Can't save logs into database.", sqle);
			// wait for some time
			try {
				Thread.sleep(2500);
			} catch (InterruptedException ex) {
				// ok just try it again
			}
			return false;
		}

		return true;
	}

	/**
	 * Fetch stored logs into database. It can be executed on user demand or by
	 * spring periodically.
	 */
	@Override
	@Async
	@Scheduled(fixedDelay = 4300)
	public synchronized void flush() {

		if (!supportsBatchUpdates) {
			// no batches, the data are stored immediately
			// we have nothing to do
			return;
		}

		// switch the logs buffers
		synchronized (lockList) {
			ArrayList<ILoggingEvent> swap = primaryList;
			primaryList = secondaryList;
			secondaryList = swap;
		}

		// do we have some logs to store?
		if (secondaryList.isEmpty()) {
			return;
		}

		Date start = new Date();

		// prepare data to fetch
		LinkedList<List<ILoggingEvent>> toFetchQueue = new LinkedList<>();
		if (secondaryList.size() < LOG_BATCH_SIZE) {
			// go all in once
			toFetchQueue.add(secondaryList);
		} else {
			// split
			int indexMax = secondaryList.size();
			LOG.debug("The logs are too big ({})spliting ..", indexMax);
			for (int index = 0; index < indexMax; index += LOG_BATCH_SIZE) {
				int topIndex = index + LOG_BATCH_SIZE < indexMax ? index + LOG_BATCH_SIZE : indexMax;
				// create sub list and add to toFetch
				toFetchQueue.add(secondaryList.subList(index, topIndex));
				LOG.debug("Created list <{}, {})", index, topIndex);
			}
		}

		// if true then we try to save data into database
		boolean nextTry = true;
		Iterator<List<ILoggingEvent>> iterator = toFetchQueue.iterator();
		// we know that the toFetchQueue has at leas one item
		List<ILoggingEvent> toFetch = iterator.next();

		while (nextTry) {
			// we do not continue in next, or at least we assume that
			// it can be changed by flushIntoDatabase
			nextTry = false;

			// get connection
			LOG.debug("flush() : get connection");
			Connection connection = getConnection();

			// update next try based on result
			// if the save failed, we try it again .. 
			while (toFetch != null) {
				// if one of them failed, then we instantly end,
				// close connection, get new one .. and give
				// it another try
				nextTry = !flushIntoDatabase(connection, toFetch);
				if (nextTry) {
					// we failed to save, give it another try
					// value of toFetch will not changed
					break;
				} else {
					// we have made it .. do we have next to save ?
					if (iterator.hasNext()) {
						// yes move and fetch
						toFetch = iterator.next();
					} else {
						// the last one\
						toFetch = null;
					}
				}
			}
			
			// at the end close the connection
			DBHelper.closeConnection(connection);
		}
		// the data has been saved, we can clear the buffer
		secondaryList.clear();

		LOG.debug("flush() -> finished in: {} ms ", (new Date()).getTime() - start.getTime());
	}

	/**
	 * Do immediate write of given log into database. If the database is down
	 * then the log is not saved.
	 *
	 * @param eventObject
	 */
	private void appendImmediate(ILoggingEvent eventObject) {
		Connection connection = null;

		try {
			connection = connectionSource.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException sqle) {
			// for sure close the connection
			DBHelper.closeConnection(connection);
			// log
			addError("failed to get sql connection", sqle);
			return;
		}

		try (PreparedStatement stmt = connection.prepareStatement(getInsertSQL())) {
			// inserting an event and getting the result must be exclusive
			synchronized (this) {
				// we can do more subAppend at time .. 
				bindStatement(eventObject, stmt);
				// execute ..
				stmt.executeUpdate();
			}
			// we no longer need the insertStatement
			stmt.close();
			// commit command
			connection.commit();
		} catch (Throwable sqle) {
			addError("problem appending event", sqle);
		} finally {
			DBHelper.closeConnection(connection);
		}
	}

	@Override
	public void start() {

		// prepare and start the connection source
		connectionSource = new LoggingConnectionSource(dataSource);
		connectionSource.setContext(this.getContext());
		connectionSource.start();

		// get information about the source
		supportsBatchUpdates = connectionSource.supportsBatchUpdates();

		started = true;
	}

	@Override
	public void append(ILoggingEvent eventObject) {

		if (supportsBatchUpdates) {
			// we can use batch .. so we just add the logs into 
			// the queue
			synchronized (lockList) {
				primaryList.add(eventObject);
			}
		} else {
			appendImmediate(eventObject);
		}
	}

	/**
	 * Bind the parameters to he insert statement.
	 *
	 * @param event
	 * @param stmt
	 * @throws Throwable
	 */
	protected void bindStatement(ILoggingEvent event,
			PreparedStatement stmt) throws Throwable {

		/* ! ! ! ! ! ! ! ! ! 
		 * As the message and stackTrace are BLOBS interpreted as string they
		 * must not be empty -> they have to be null or have some content
		 *
		 */
		// prepare the values
		Integer logLevel = event.getLevel().toInteger();
		long timeStamp = event.getTimeStamp();
		String logger = StringUtils.abbreviate(event.getLoggerName(), LenghtLimits.LOGGER_NAME.limit());
		String message = event.getFormattedMessage();

		// null check
		if (logLevel == null) {
			logLevel = Level.INFO_INTEGER;
		}
		if (logger == null) {
			logger = "unknown";
		}

		// bind
		stmt.setInt(1, logLevel);
		stmt.setLong(2, timeStamp);
		stmt.setString(3, logger);
		stmt.setString(4, message);

		// get DPU and EXECUTION from MDC
		final Map<String, String> mdc = event.getMDCPropertyMap();

		try {
			final String dpuString = mdc.get(Log.MDC_DPU_INSTANCE_KEY_NAME);
			final int dpuId = Integer.parseInt(dpuString);
			stmt.setInt(5, dpuId);
		} catch (NumberFormatException | SQLException ex) {
			stmt.setNull(5, java.sql.Types.INTEGER);
		}

		try {
			final String execString = mdc.get(Log.MDC_EXECUTION_KEY_NAME);
			final int execId = Integer.parseInt(execString);
			stmt.setInt(6, execId);
		} catch (NumberFormatException | SQLException ex) {
			stmt.setNull(6, java.sql.Types.INTEGER);
			LOG.error("Failed to set EXECUTION_KEY for log.", ex);
		}

		String stackTrace = null;
		IThrowableProxy proxy = event.getThrowableProxy();
		if (proxy != null) {
			StringBuilder sb = new StringBuilder();
			prepareStackTrace(proxy, sb);
			stackTrace = sb.toString();
		}

		stmt.setString(7, stackTrace);
	}

	/**
	 * Convert information about stack trace into text form.
	 *
	 * @param proxy
	 * @param sb
	 */
	private void prepareStackTrace(IThrowableProxy proxy, StringBuilder sb) {
		sb.append(proxy.getClassName());
		sb.append(' ');
		sb.append(proxy.getMessage());
		sb.append('\n');

		final StackTraceElementProxy[] stack
				= proxy.getStackTraceElementProxyArray();
		int index = proxy.getCommonFrames();
		if (index != 0) {
			sb.append("   ... ");
			sb.append(index);
			sb.append(" common frames omitted");
		}
		final int indexEnd = stack.length;
		for (; index < indexEnd; ++index) {
			sb.append("   ");
			sb.append(stack[index].getSTEAsString());
			sb.append('\n');
		}

		if (proxy.getCause() != null) {
			prepareStackTrace(proxy.getCause(), sb);
		}
	}

}
