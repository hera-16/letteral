package com.chatapp.model;

/**
 * 権限階級名のEnum
 * Letteral仕様: 社長 > 部長 > 課長 > PM > 一般メンバー
 */
public enum RoleName {
    /**
     * 社長（レベル100）
     * 全社の最高責任者。全てのBoxを閲覧可能。
     */
    PRESIDENT("社長", 100),

    /**
     * 部長（レベル80）
     * 部門の責任者。部門以下のBoxを閲覧可能。
     */
    DIRECTOR("部長", 80),

    /**
     * 課長（レベル60）
     * 課の責任者。課以下のBoxを閲覧可能。
     */
    MANAGER("課長", 60),

    /**
     * プロジェクトマネージャー（レベル40）
     * プロジェクトの責任者。プロジェクト内のBoxを閲覧可能。
     */
    PM("プロジェクトマネージャー", 40),

    /**
     * 一般メンバー（レベル20）
     * 一般的なメンバー。自分のBoxのみ閲覧可能。
     */
    MEMBER("一般メンバー", 20);

    private final String displayName;
    private final int level;

    RoleName(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    /**
     * レベルから対応するRoleNameを取得
     */
    public static RoleName fromLevel(int level) {
        for (RoleName roleName : values()) {
            if (roleName.level == level) {
                return roleName;
            }
        }
        throw new IllegalArgumentException("Invalid role level: " + level);
    }

    /**
     * 文字列から対応するRoleNameを取得
     */
    public static RoleName fromString(String name) {
        for (RoleName roleName : values()) {
            if (roleName.name().equalsIgnoreCase(name)) {
                return roleName;
            }
        }
        throw new IllegalArgumentException("Invalid role name: " + name);
    }

    /**
     * このロールが指定されたロールより上位かどうか
     */
    public boolean isHigherThan(RoleName other) {
        return this.level > other.level;
    }

    /**
     * このロールが指定されたロールより下位かどうか
     */
    public boolean isLowerThan(RoleName other) {
        return this.level < other.level;
    }

    /**
     * このロールが指定されたロール以上かどうか
     */
    public boolean isAtLeast(RoleName other) {
        return this.level >= other.level;
    }
}
