package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Detects application status changes and delivers in-app notifications to TAs.
 */
public class TaNotificationService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final SessionManager sessionManager;

    public TaNotificationService(
        ApplicationRepository applicationRepository,
        PositionRepository positionRepository,
        SessionManager sessionManager
    ) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * Compare current application statuses with checkpoints and create notifications for changes.
     */
    public int syncNotificationsForUser(String userId) {
        Map<String, String> checkpoints = TaApplicationStatusCheckpointStore.readForUser(userId);
        Map<String, String> latest = new HashMap<>();
        int created = 0;

        for (Application app : applicationRepository.findByApplicant(userId)) {
            String appId = app.getApplicationId();
            String currentStatus = app.getStatus().name();
            latest.put(appId, currentStatus);

            String previous = checkpoints.get(appId);
            if (previous == null) {
                continue;
            }
            if (!previous.equals(currentStatus)) {
                createNotification(userId, app, previous, currentStatus);
                created++;
            }
        }

        TaApplicationStatusCheckpointStore.writeForUser(userId, latest);
        return created;
    }

    public int syncNotifications(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        return syncNotificationsForUser(session.getUserId());
    }

    public List<TaNotification> listNotifications(String token, boolean unreadOnly) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        syncNotificationsForUser(session.getUserId());

        List<TaNotification> list = TaNotificationStore.findByUser(session.getUserId());
        if (unreadOnly) {
            list.removeIf(TaNotification::isRead);
        }
        list.sort(Comparator.comparing(TaNotification::getCreatedAt).reversed());
        return list;
    }

    public int countUnread(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        syncNotificationsForUser(session.getUserId());
        int count = 0;
        for (TaNotification n : TaNotificationStore.findByUser(session.getUserId())) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markAsRead(String token, String notificationId) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        for (TaNotification n : TaNotificationStore.findByUser(session.getUserId())) {
            if (n.getNotificationId().equals(notificationId)) {
                n.setRead(true);
                TaNotificationStore.save(n);
                return;
            }
        }
    }

    public void markAllAsRead(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        for (TaNotification n : TaNotificationStore.findByUser(session.getUserId())) {
            if (!n.isRead()) {
                n.setRead(true);
                TaNotificationStore.save(n);
            }
        }
    }

    private void createNotification(String userId, Application app, String oldStatus, String newStatus) {
        Optional<Position> position = positionRepository.findById(app.getPositionId());
        String jobTitle = position.map(Position::getJobTitle).orElse(app.getPositionId());

        TaNotification notification = new TaNotification();
        notification.setNotificationId(IdGenerator.nextId("NTF"));
        notification.setUserId(userId);
        notification.setApplicationId(app.getApplicationId());
        notification.setPositionId(app.getPositionId());
        notification.setOldStatus(oldStatus);
        notification.setNewStatus(newStatus);
        notification.setCreatedAt(LocalDateTime.now().format(FORMAT));
        notification.setRead(false);

        String note = app.getStatusNote();
        StringBuilder message = new StringBuilder();
        message.append("Application ").append(app.getApplicationId());
        message.append(" (").append(jobTitle).append("): ");
        message.append(oldStatus).append(" → ").append(newStatus);
        if (note != null && !note.trim().isEmpty()) {
            message.append(". Note: ").append(note.trim());
        }
        notification.setMessage(message.toString());
        TaNotificationStore.save(notification);
    }
}
