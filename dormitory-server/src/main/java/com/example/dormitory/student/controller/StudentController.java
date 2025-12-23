package com.example.dormitory.student.controller;

import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.student.dto.CreateStudentRequest;
import com.example.dormitory.student.dto.UpdateStudentRequest;
import com.example.dormitory.student.service.StudentService;
import com.example.dormitory.student.vo.StudentVo;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('student:read')")
    public ApiResponse<PageResult<StudentVo>> list(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder
    ) {
        return ApiResponse.ok(studentService.list(gender, status, keyword, page, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('student:read')")
    public ApiResponse<StudentVo> get(@PathVariable Long id) {
        return ApiResponse.ok(studentService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('student:write')")
    public ApiResponse<StudentVo> create(@Valid @RequestBody CreateStudentRequest req) {
        return ApiResponse.ok(studentService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('student:write')")
    public ApiResponse<StudentVo> update(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest req) {
        return ApiResponse.ok(studentService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('student:write')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ApiResponse.ok(null);
    }
}
