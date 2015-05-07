package com.fdt.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fdt.security.entity.EComAdminUser;

@Service("eComAdminUserDetailsService")
public class EComAdminUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EComAdminUserService eComAdminUserService = null;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EComAdminUser user = this.eComAdminUserService.loadUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }
        username = user.getUsername();
        return user;
    }
}
