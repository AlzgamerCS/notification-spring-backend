package com.nurtore.notification_spring.dto;

import com.nurtore.notification_spring.model.NotificationChannel;
import com.nurtore.notification_spring.model.NotificationPreference;
import lombok.Data;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
public class NotificationPreferenceDTO {
    private UUID id;
    private UUID userId;
    private NotificationChannel channel;
    private Set<Integer> leadDays;
    private LocalTime dailyTime;
    private Boolean enabled;

    public static NotificationPreferenceDTO fromEntity(NotificationPreference preference) {
        NotificationPreferenceDTO dto = new NotificationPreferenceDTO();
        dto.setId(preference.getId());
        dto.setUserId(preference.getUser().getId());
        dto.setChannel(preference.getChannel());
        dto.setLeadDays(preference.getLeadDays());
        dto.setDailyTime(preference.getDailyTime());
        dto.setEnabled(preference.getEnabled());
        return dto;
    }
} 