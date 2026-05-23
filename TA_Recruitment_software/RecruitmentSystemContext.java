package TA_Recruitment_software;

import TA_Recruitment_software.admin_system.AdminService;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.AuthService;
import TA_Recruitment_software.auth.SessionManager;
import TA_Recruitment_software.mo_publish.MOPublishService;
import TA_Recruitment_software.mo_review.MOReviewService;
<<<<<<< Updated upstream
=======
import TA_Recruitment_software.ta_jobs.AiDiagnosisService;
import TA_Recruitment_software.profile.CoverLetterService;
>>>>>>> Stashed changes
import TA_Recruitment_software.profile.MoTaProfileViewService;
import TA_Recruitment_software.profile.ProfileExportFacade;
import TA_Recruitment_software.profile.ProfileService;
import TA_Recruitment_software.ta_jobs.TAJobService;

public class RecruitmentSystemContext {
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final SessionManager sessionManager;

    private final AuthService authService;
    private final ProfileService profileService;
    private final CoverLetterService coverLetterService;
    private final ProfileExportFacade profileExportFacade;
    private final MoTaProfileViewService moTaProfileViewService;
    private final TAJobService taJobService;
    private final MOPublishService moPublishService;
    private final MOReviewService moReviewService;
    private final AdminService adminService;

    public RecruitmentSystemContext() {
        this.userRepository = new UserRepository();
        this.positionRepository = new PositionRepository();
        this.applicationRepository = new ApplicationRepository();
        this.sessionManager = new SessionManager();

        this.authService = new AuthService(userRepository, sessionManager);
        this.profileService = new ProfileService(userRepository, sessionManager);
        this.coverLetterService = new CoverLetterService(sessionManager);
        this.profileExportFacade = new ProfileExportFacade(userRepository, sessionManager, profileService);
        this.moTaProfileViewService = new MoTaProfileViewService(userRepository, sessionManager);
        this.taJobService = new TAJobService(positionRepository, applicationRepository, userRepository, sessionManager);
        this.moPublishService = new MOPublishService(positionRepository, userRepository, sessionManager);
        this.moReviewService = new MOReviewService(positionRepository, applicationRepository, userRepository, sessionManager);
        this.adminService = new AdminService(userRepository, positionRepository, applicationRepository, sessionManager);
        this.adminService.seedDefaultAdmin();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public CoverLetterService getCoverLetterService() {
        return coverLetterService;
    }

    public ProfileExportFacade getProfileExportFacade() {
        return profileExportFacade;
    }

    public MoTaProfileViewService getMoTaProfileViewService() {
        return moTaProfileViewService;
    }

    public TAJobService getTaJobService() {
        return taJobService;
    }

    public MOPublishService getMoPublishService() {
        return moPublishService;
    }

    public MOReviewService getMoReviewService() {
        return moReviewService;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
