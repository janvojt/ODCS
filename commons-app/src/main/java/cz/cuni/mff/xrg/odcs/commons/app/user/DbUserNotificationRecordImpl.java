package cz.cuni.mff.xrg.odcs.commons.app.user;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation providing access to {@link UserNotificationRecord} data objects.
 *
 * @author Jan Vojt
 */
@Transactional(propagation = Propagation.MANDATORY)
class DbUserNotificationRecordImpl extends DbAccessBase<UserNotificationRecord>
											implements DbUserNotification {

	public DbUserNotificationRecordImpl() {
		super(UserNotificationRecord.class);
	}

}
