package com.example.disastermod;

public enum DisasterType {
    METEOR,
    EARTHQUAKE,
    TSUNAMI;

    // 文字列(meteorなど)からEnumを取得するヘルパーメソッド
    public static DisasterType fromString(String name) {
        for (DisasterType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}