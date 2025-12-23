package com.example.dormitory.dorm.service;

import com.example.dormitory.common.BizException;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.dto.CreateDormBuildingRequest;
import com.example.dormitory.dorm.dto.UpdateDormBuildingRequest;
import com.example.dormitory.dorm.model.DormBuilding;
import com.example.dormitory.dorm.repository.DormBuildingRepository;
import com.example.dormitory.dorm.vo.DormBuildingVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormBuildingService {

    private final DormBuildingRepository repository;

    public DormBuildingService(DormBuildingRepository repository) {
        this.repository = repository;
    }

    public PageResult<DormBuildingVo> list(String keyword, String genderLimit, Integer page, Integer pageSize, String sortBy, String sortOrder) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);

        String orderBy = resolveOrderBy(sortBy, sortOrder);
        int offset = (p - 1) * ps;

        long total = repository.count(keyword, genderLimit);
        List<DormBuilding> list = repository.list(keyword, genderLimit, orderBy, ps, offset);
        List<DormBuildingVo> voList = list.stream().map(this::toVo).toList();
        return new PageResult<>(voList, p, ps, total);
    }

    public DormBuildingVo get(Long id) {
        DormBuilding b = repository.findById(id);
        if (b == null) {
            throw new BizException(40004, "building not found");
        }
        return toVo(b);
    }

    public DormBuildingVo create(CreateDormBuildingRequest req) {
        DormBuilding b = new DormBuilding();
        b.setCode(req.getCode().trim());
        b.setName(req.getName().trim());
        b.setGenderLimit(req.getGenderLimit().trim());
        b.setAddress(req.getAddress());
        b.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "ACTIVE" : req.getStatus().trim());
        Long id = repository.insert(b);
        if (id == null) {
            throw new BizException(50000, "create building failed");
        }
        return get(id);
    }

    public DormBuildingVo update(Long id, UpdateDormBuildingRequest req) {
        DormBuilding b = new DormBuilding();
        b.setCode(req.getCode().trim());
        b.setName(req.getName().trim());
        b.setGenderLimit(req.getGenderLimit().trim());
        b.setAddress(req.getAddress());
        b.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "ACTIVE" : req.getStatus().trim());
        int affected = repository.update(id, b);
        if (affected == 0) {
            throw new BizException(40004, "building not found");
        }
        return get(id);
    }

    public void delete(Long id) {
        int affected = repository.softDelete(id);
        if (affected == 0) {
            throw new BizException(40004, "building not found");
        }
    }

    private DormBuildingVo toVo(DormBuilding b) {
        DormBuildingVo vo = new DormBuildingVo();
        vo.setId(b.getId());
        vo.setCode(b.getCode());
        vo.setName(b.getName());
        vo.setGenderLimit(b.getGenderLimit());
        vo.setAddress(b.getAddress());
        vo.setStatus(b.getStatus());
        return vo;
    }

    private String resolveOrderBy(String sortBy, String sortOrder) {
        String column;
        if ("code".equalsIgnoreCase(sortBy)) {
            column = "code";
        } else if ("name".equalsIgnoreCase(sortBy)) {
            column = "name";
        } else if ("genderLimit".equalsIgnoreCase(sortBy) || "gender_limit".equalsIgnoreCase(sortBy)) {
            column = "gender_limit";
        } else if ("status".equalsIgnoreCase(sortBy)) {
            column = "status";
        } else {
            column = "id";
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
