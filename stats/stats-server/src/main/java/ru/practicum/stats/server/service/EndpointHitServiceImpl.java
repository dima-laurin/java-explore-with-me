package ru.practicum.stats.server.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.mapper.EndpointHitMapper;
import ru.practicum.stats.server.repository.EndpointHitRepository;
import ru.practicum.stats.server.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;

    public EndpointHitServiceImpl(EndpointHitRepository endpointHitRepository) {
        this.endpointHitRepository = endpointHitRepository;
    }

    @Override
    @Transactional
    public void addHit(EndpointHitDto endpointHitDto) {
        endpointHitRepository.save(
                EndpointHitMapper.toEndpointHit(endpointHitDto)
        );
    }

    @Override
    public List<ViewStatsDto> viewStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        if (start.isAfter(end)) {
            throw new BadRequestException(
                    "Дата начала не может быть позже даты окончания"
            );
        }

        boolean urisEmpty;

        if (uris == null) {
            urisEmpty = true;
        } else {
            urisEmpty = uris.isEmpty();
        }

        if (urisEmpty && !unique) {
            return endpointHitRepository.findStats(start, end);
        }

        if (urisEmpty) {
            return endpointHitRepository.findUniqueStats(start, end);
        }

        if (!unique) {
            return endpointHitRepository.findStatsByUris(start, end, uris);
        }

        return endpointHitRepository.findUniqueStatsByUris(start, end, uris);
    }
}
