package com.reything.subsmentbe.domain.enums;

public enum CategoryType {
    Eglence("Eğlence", "🎬", "#FF6B6B"),
    Muzik("Müzik", "🎵", "#4ECDC4"),
    BulutDepolama("Bulut Depolama", "☁️", "#45B7D1"),
    Yazilim("Yazılım", "💻", "#96CEB4"),
    Spor("Spor", "🏃", "#FFA07A"),
    Oyun("Oyun", "🎮", "#9B59B6"),
    Haberler("Haberler", "📰", "#34495E"),
    Egitim("Eğitim", "📚", "#F39C12"),
    Diger("Diğer", "📦", "#95A5A6");

    private final String displayName;
    private final String emoji;
    private final String color;

    CategoryType(String displayName, String emoji, String color) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getColor() {
        return color;
    }
}
