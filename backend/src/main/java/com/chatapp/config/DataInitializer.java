package com.chatapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.Organization;
import com.chatapp.model.OrganizationMember;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.OrganizationRole;
import com.chatapp.repository.OrganizationMemberRepository;
import com.chatapp.repository.OrganizationRepository;
import com.chatapp.repository.TenantRepository;
import com.chatapp.repository.UserRepository;

/**
 * アプリケーション起動時に初期データを投入するコンポーネント。
 * data.sql の実行が環境に依存してしまうケースがあるため、
 * JPA を通して確実にデータを整備する。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TenantRepository tenantRepository,
                           OrganizationRepository organizationRepository,
                           OrganizationMemberRepository organizationMemberRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 既存データを完全削除
        LOGGER.info("Deleting all existing data...");
        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        tenantRepository.deleteAll();
        userRepository.deleteAll();
        LOGGER.info("All existing data deleted");

        // 新しいテストデータを作成
        seedTestData();
    }

    /**
     * テストデータを作成
     * 社長1人、部長2人、課長4人（各部署に2人）、PM1人、一般ユーザー3人
     */
    private void seedTestData() {
        LOGGER.info("Creating test data...");

        // 1. テナント作成
        Tenant tenant = new Tenant("テストカンパニー株式会社", "test-company");
        tenant.setContactEmail("contact@test-company.com");
        tenant = tenantRepository.save(tenant);
        LOGGER.info("Created tenant: {}", tenant.getName());

        // 2. 組織階層作成
        // 会社（ルート組織）
        Organization company = new Organization(tenant, "テストカンパニー株式会社");
        company.setOrganizationType("COMPANY");
        company.setLevel(1);
        company.setPath("/1");
        company = organizationRepository.save(company);
        LOGGER.info("Created company organization: {}", company.getName());

        // 第1営業部
        Organization dept1 = new Organization(tenant, "第1営業部");
        dept1.setOrganizationType("DEPARTMENT");
        dept1.setParent(company);
        dept1.setLevel(2);
        dept1.setPath(company.getPath() + "/" + (company.getId() + 1));
        dept1 = organizationRepository.save(dept1);
        LOGGER.info("Created department: {}", dept1.getName());

        // 第2営業部
        Organization dept2 = new Organization(tenant, "第2営業部");
        dept2.setOrganizationType("DEPARTMENT");
        dept2.setParent(company);
        dept2.setLevel(2);
        dept2.setPath(company.getPath() + "/" + (company.getId() + 2));
        dept2 = organizationRepository.save(dept2);
        LOGGER.info("Created department: {}", dept2.getName());

        // 第1営業部 - 第1課
        Organization section1_1 = new Organization(tenant, "第1課");
        section1_1.setOrganizationType("SECTION");
        section1_1.setParent(dept1);
        section1_1.setLevel(3);
        section1_1.setPath(dept1.getPath() + "/" + (dept1.getId() + 1));
        section1_1 = organizationRepository.save(section1_1);
        LOGGER.info("Created section: {} (under {})", section1_1.getName(), dept1.getName());

        // 第1営業部 - 第2課
        Organization section1_2 = new Organization(tenant, "第2課");
        section1_2.setOrganizationType("SECTION");
        section1_2.setParent(dept1);
        section1_2.setLevel(3);
        section1_2.setPath(dept1.getPath() + "/" + (dept1.getId() + 2));
        section1_2 = organizationRepository.save(section1_2);
        LOGGER.info("Created section: {} (under {})", section1_2.getName(), dept1.getName());

        // 第2営業部 - 第1課
        Organization section2_1 = new Organization(tenant, "第1課");
        section2_1.setOrganizationType("SECTION");
        section2_1.setParent(dept2);
        section2_1.setLevel(3);
        section2_1.setPath(dept2.getPath() + "/" + (dept2.getId() + 1));
        section2_1 = organizationRepository.save(section2_1);
        LOGGER.info("Created section: {} (under {})", section2_1.getName(), dept2.getName());

        // 第2営業部 - 第2課
        Organization section2_2 = new Organization(tenant, "第2課");
        section2_2.setOrganizationType("SECTION");
        section2_2.setParent(dept2);
        section2_2.setLevel(3);
        section2_2.setPath(dept2.getPath() + "/" + (dept2.getId() + 2));
        section2_2 = organizationRepository.save(section2_2);
        LOGGER.info("Created section: {} (under {})", section2_2.getName(), dept2.getName());

        // プロジェクトチーム（第1営業部 第1課 配下）
        Organization projectTeam = new Organization(tenant, "新規事業プロジェクト");
        projectTeam.setOrganizationType("TEAM");
        projectTeam.setParent(section1_1);
        projectTeam.setLevel(4);
        projectTeam.setPath(section1_1.getPath() + "/" + (section1_1.getId() + 1));
        projectTeam = organizationRepository.save(projectTeam);
        LOGGER.info("Created team: {} (under {})", projectTeam.getName(), section1_1.getName());

        // 3. ユーザー作成
        // 社長
        User ceo = createUser("ceo", "ceo@test-company.com", "山田太郎", "password");
        LOGGER.info("Created user: {} (社長)", ceo.getDisplayName());

        // 部長2人
        User manager1 = createUser("manager1", "manager1@test-company.com", "佐藤一郎", "password");
        User manager2 = createUser("manager2", "manager2@test-company.com", "鈴木二郎", "password");
        LOGGER.info("Created users: {} (第1営業部長), {} (第2営業部長)",
                    manager1.getDisplayName(), manager2.getDisplayName());

        // 課長4人
        User chief1_1 = createUser("chief1_1", "chief1_1@test-company.com", "田中三郎", "password");
        User chief1_2 = createUser("chief1_2", "chief1_2@test-company.com", "高橋四郎", "password");
        User chief2_1 = createUser("chief2_1", "chief2_1@test-company.com", "伊藤五郎", "password");
        User chief2_2 = createUser("chief2_2", "chief2_2@test-company.com", "渡辺六郎", "password");
        LOGGER.info("Created users: {} (第1営業部第1課長), {} (第1営業部第2課長), {} (第2営業部第1課長), {} (第2営業部第2課長)",
                    chief1_1.getDisplayName(), chief1_2.getDisplayName(),
                    chief2_1.getDisplayName(), chief2_2.getDisplayName());

        // PM1人
        User pm = createUser("pm001", "pm@test-company.com", "中村七郎", "password");
        LOGGER.info("Created user: {} (プロジェクトマネージャー)", pm.getDisplayName());

        // 一般ユーザー3人
        User member1 = createUser("member1", "member1@test-company.com", "小林八郎", "password");
        User member2 = createUser("member2", "member2@test-company.com", "加藤九郎", "password");
        User member3 = createUser("member3", "member3@test-company.com", "吉田十郎", "password");
        LOGGER.info("Created users: {} (一般), {} (一般), {} (一般)",
                    member1.getDisplayName(), member2.getDisplayName(), member3.getDisplayName());

        // 4. 組織メンバーシップ作成
        // 社長 - 会社全体の管理者
        createOrgMember(tenant, company, ceo, OrganizationRole.ADMIN_ROOT, true);

        // 部長 - 各部の管理者
        createOrgMember(tenant, dept1, manager1, OrganizationRole.ADMIN_CORE, true);
        createOrgMember(tenant, dept2, manager2, OrganizationRole.ADMIN_CORE, true);

        // 課長 - 各課の管理者
        createOrgMember(tenant, section1_1, chief1_1, OrganizationRole.ADMIN_LEAD, true);
        createOrgMember(tenant, section1_2, chief1_2, OrganizationRole.ADMIN_LEAD, true);
        createOrgMember(tenant, section2_1, chief2_1, OrganizationRole.ADMIN_LEAD, true);
        createOrgMember(tenant, section2_2, chief2_2, OrganizationRole.ADMIN_LEAD, true);

        // PM - プロジェクトチームの管理者
        createOrgMember(tenant, projectTeam, pm, OrganizationRole.ADMIN_SUPER, true);

        // 一般ユーザー - プロジェクトチームのメンバー
        createOrgMember(tenant, projectTeam, member1, OrganizationRole.MEMBER, false);
        createOrgMember(tenant, projectTeam, member2, OrganizationRole.MEMBER, false);
        createOrgMember(tenant, projectTeam, member3, OrganizationRole.MEMBER, false);

        LOGGER.info("Test data creation completed successfully!");
    }

    private User createUser(String username, String email, String displayName, String password) {
        User user = new User(username, email, passwordEncoder.encode(password), displayName);
        return userRepository.save(user);
    }

    private OrganizationMember createOrgMember(Tenant tenant, Organization org, User user,
                                                OrganizationRole role, boolean isPrimary) {
        OrganizationMember member = new OrganizationMember(tenant, org, user);
        member.setRole(role);
        member.setIsPrimary(isPrimary);
        OrganizationMember saved = organizationMemberRepository.save(member);
        LOGGER.info("Added {} to {} with role {}", user.getDisplayName(), org.getName(), role);
        return saved;
    }
}
