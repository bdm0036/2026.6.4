package com.bookstore.user.controller;

import com.bookstore.common.entity.Result;
import com.bookstore.common.utils.UserContext;
import com.bookstore.user.service.BrowseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user/history")
@RequiredArgsConstructor
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    @PostMapping("/{bookId}")
    public Result<Void> record(@PathVariable Long bookId) {
        Long userId = UserContext.getUserId();
        browseHistoryService.record(userId, bookId);
        return Result.success();
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "12") Integer size) {
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> result = browseHistoryService.list(userId, page, size);
        return Result.success(result);
    }

    @DeleteMapping
    public Result<Void> clear() {
        Long userId = UserContext.getUserId();
        browseHistoryService.clear(userId);
        return Result.success("已清除");
    }
}
