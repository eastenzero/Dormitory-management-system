package com.example.dormitory.dorm.service;

import com.example.dormitory.common.BizException;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.dto.CreateDormBedRequest;
import com.example.dormitory.dorm.dto.UpdateDormBedRequest;
import com.example.dormitory.dorm.model.DormBed;
import com.example.dormitory.dorm.repository.DormBedRepository;
import com.example.dormitory.dorm.vo.DormBedVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormBedService {

    private final DormBedRepository repository;

    public DormBedService(DormBedRepository repository) {
        this.repository = repository;
    }

    public PageResult<DormBedVo> list(Long buildingId, Long roomId, String status, String keyword,
                                     Integer page, Integer pageSize, String sortBy, String sortOrder) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
        int offset = (p - 1) * ps;

        String orderBy = resolveOrderBy(sortBy, sortOrder);
        long total = repository.count(buildingId, roomId, status, keyword);
        List<DormBedVo> list = repository.list(buildingId, roomId, status, keyword, orderBy, ps, offset);
        return new PageResult<>(list, p, ps, total);
    }

    public DormBedVo get(Long id) {
        DormBedVo vo = repository.findVoById(id);
        if (vo == null) {
            throw new BizException(40004, "bed not found");
        }
        return vo;
    }

    public DormBedVo create(CreateDormBedRequest req) {
        DormBed bed = new DormBed();
        bed.setRoomId(req.getRoomId());
        bed.setBedNo(req.getBedNo().trim());
        bed.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "AVAILABLE" : req.getStatus().trim());
        Long id = repository.insert(bed);
        if (id == null) {
            throw new BizException(50000, "create bed failed");
        }
        return get(id);
    }

    public DormBedVo update(Long id, UpdateDormBedRequest req) {
        DormBed bed = new DormBed();
        bed.setRoomId(req.getRoomId());
        bed.setBedNo(req.getBedNo().trim());
        bed.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "AVAILABLE" : req.getStatus().trim());
        int affected = repository.update(id, bed);
        if (affected == 0) {
            throw new BizException(40004, "bed not found");
        }
        return get(id);
    }

    public void delete(Long id) {
        int affected = repository.softDelete(id);
        if (affected == 0) {
            throw new BizException(40004, "bed not found");
        }
    }

    private String resolveOrderBy(String sortBy, String sortOrder) {
        String column;
        if ("bedNo".equalsIgnoreCase(sortBy) || "bed_no".equalsIgnoreCase(sortBy)) {
            column = "bed.bed_no";
        } else if ("roomId".equalsIgnoreCase(sortBy) || "room_id".equalsIgnoreCase(sortBy)) {
            column = "bed.room_id";
        } else if ("status".equalsIgnoreCase(sortBy)) {
            column = "bed.status";
        } else {
            column = "bed.id";
        }

        String order = "desc";
        if ("asc".equalsIgnoreCase(sortOrder)) {
            order = "asc";
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            order = "desc";
        }
        return column + " " + order;
    }
}
