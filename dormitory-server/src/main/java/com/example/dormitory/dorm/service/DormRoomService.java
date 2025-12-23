package com.example.dormitory.dorm.service;

import com.example.dormitory.common.BizException;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.dto.CreateDormRoomRequest;
import com.example.dormitory.dorm.dto.UpdateDormRoomRequest;
import com.example.dormitory.dorm.model.DormRoom;
import com.example.dormitory.dorm.repository.DormRoomRepository;
import com.example.dormitory.dorm.vo.DormRoomVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormRoomService {

    private final DormRoomRepository repository;

    public DormRoomService(DormRoomRepository repository) {
        this.repository = repository;
    }

    public PageResult<DormRoomVo> list(Long buildingId, Integer floorNo, String roomNo, String status, String keyword,
                                      Integer page, Integer pageSize, String sortBy, String sortOrder) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
        int offset = (p - 1) * ps;

        String orderBy = resolveOrderBy(sortBy, sortOrder);
        long total = repository.count(buildingId, floorNo, roomNo, status, keyword);
        List<DormRoomVo> list = repository.list(buildingId, floorNo, roomNo, status, keyword, orderBy, ps, offset);
        return new PageResult<>(list, p, ps, total);
    }

    public DormRoomVo get(Long id) {
        DormRoomVo vo = repository.findVoById(id);
        if (vo == null) {
            throw new BizException(40004, "room not found");
        }
        return vo;
    }

    public DormRoomVo create(CreateDormRoomRequest req) {
        DormRoom room = new DormRoom();
        room.setBuildingId(req.getBuildingId());
        room.setFloorNo(req.getFloorNo());
        room.setRoomNo(req.getRoomNo().trim());
        room.setRoomType(req.getRoomType());
        room.setGenderLimit(req.getGenderLimit() == null || req.getGenderLimit().isBlank() ? "UNLIMITED" : req.getGenderLimit().trim());
        room.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "ACTIVE" : req.getStatus().trim());
        Long id = repository.insert(room);
        if (id == null) {
            throw new BizException(50000, "create room failed");
        }
        return get(id);
    }

    public DormRoomVo update(Long id, UpdateDormRoomRequest req) {
        DormRoom room = new DormRoom();
        room.setBuildingId(req.getBuildingId());
        room.setFloorNo(req.getFloorNo());
        room.setRoomNo(req.getRoomNo().trim());
        room.setRoomType(req.getRoomType());
        room.setGenderLimit(req.getGenderLimit() == null || req.getGenderLimit().isBlank() ? "UNLIMITED" : req.getGenderLimit().trim());
        room.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "ACTIVE" : req.getStatus().trim());
        int affected = repository.update(id, room);
        if (affected == 0) {
            throw new BizException(40004, "room not found");
        }
        return get(id);
    }

    public void delete(Long id) {
        int affected = repository.softDelete(id);
        if (affected == 0) {
            throw new BizException(40004, "room not found");
        }
    }

    private String resolveOrderBy(String sortBy, String sortOrder) {
        String column;
        if ("roomNo".equalsIgnoreCase(sortBy) || "room_no".equalsIgnoreCase(sortBy)) {
            column = "r.room_no";
        } else if ("floorNo".equalsIgnoreCase(sortBy) || "floor_no".equalsIgnoreCase(sortBy)) {
            column = "r.floor_no";
        } else if ("buildingId".equalsIgnoreCase(sortBy) || "building_id".equalsIgnoreCase(sortBy)) {
            column = "r.building_id";
        } else if ("status".equalsIgnoreCase(sortBy)) {
            column = "r.status";
        } else {
            column = "r.id";
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
