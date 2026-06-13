package com.bookstore.user.controller;

import com.bookstore.common.entity.Result;
import com.bookstore.user.entity.Address;
import com.bookstore.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public Result<List<Address>> list() {
        return Result.success(addressService.listByUser());
    }

    @GetMapping("/{id}")
    public Result<Address> get(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    @PostMapping
    public Result<Address> add(@RequestBody Address address) {
        return Result.success("添加成功", addressService.add(address));
    }

    @PutMapping("/{id}")
    public Result<Address> update(@PathVariable Long id, @RequestBody Address address) {
        return Result.success("更新成功", addressService.update(id, address));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success("删除成功");
    }
}
