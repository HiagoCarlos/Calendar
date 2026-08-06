package br.com.calendar.configuration;

import br.com.calendar.configuration.dto.ConfigurationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private static final String DEFAULT_LANGUAGE = "pt-BR";
    private static final Theme DEFAULT_THEME = Theme.light;
    private static final TimeFormat DEFAULT_TIME_FORMAT = TimeFormat.h24;
    private static final Short DEFAULT_WEEK_START_DAY = 1; // 1 = segunda-feira (ISO 8601)
    private static final DefaultView DEFAULT_VIEW = DefaultView.month;

    private final ConfigurationRepository configurationRepository;
    private final ConfigurationMapper configurationMapper;

    public ConfigurationResponseDTO createDefaultConfiguration(String userId) {
        Configuration configuration = new Configuration();
        configuration.setId(userId);
        configuration.setLanguage(DEFAULT_LANGUAGE);
        configuration.setTheme(DEFAULT_THEME);
        configuration.setTimeFormat(DEFAULT_TIME_FORMAT);
        configuration.setWeekStartDay(DEFAULT_WEEK_START_DAY);
        configuration.setDefaultView(DEFAULT_VIEW);

        Configuration saved = configurationRepository.save(configuration);
        return configurationMapper.toResponse(saved);
    }
}