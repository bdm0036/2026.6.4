package com.bookstore.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.common.utils.UserContext;
import com.bookstore.user.entity.Address;
import com.bookstore.user.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    public List<Address> listByUser() {
        Long userId = UserContext.getUserId();
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime));
    }

    public Address getById(Long id) {
        return addressMapper.selectById(id);
    }

    @Transactional
    public Address add(Address address) {
        address.setUserId(UserContext.getUserId());
        address.setId(null);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        addressMapper.insert(address);
        return address;
    }

    @Transactional
    public Address update(Long id, Address address) {
        Address existing = addressMapper.selectById(id);
        if (existing == null) throw new RuntimeException("地址不存在");
        address.setId(id);
        address.setUserId(existing.getUserId());
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(existing.getUserId());
        }
        addressMapper.updateById(address);
        return addressMapper.selectById(id);
    }

    @Transactional
    public void delete(Long id) {
        Address addr = addressMapper.selectById(id);
        if (addr == null) throw new RuntimeException("地址不存在");
        if (!addr.getUserId().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权删除");
        }
        addressMapper.deleteById(id);
    }

    private void clearDefault(Long userId) {
        List<Address> defaults = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId).eq(Address::getIsDefault, 1));
        for (Address a : defaults) {
            a.setIsDefault(0);
            addressMapper.updateById(a);
        }
    }
}
