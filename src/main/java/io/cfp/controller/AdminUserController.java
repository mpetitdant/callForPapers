/*
 * Copyright (c) 2016 BreizhCamp
 * [http://breizhcamp.org]
 *
 * This file is part of CFP.io.
 *
 * CFP.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.cfp.controller;

import io.cfp.dto.AdminUserInfo;
import io.cfp.entity.Event;
import io.cfp.entity.Role;
import io.cfp.entity.User;
import io.cfp.repository.RoleRepository;
import io.cfp.service.auth.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = { "/v0/adminUser", "/api/adminUser" }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AdminUserController {

    @Autowired
    private RoleRepository roles;

    @Autowired
    private AuthUtils authUtils;

    /**
     * Obtain current admin user information
     */
    @RequestMapping(value="/currentUser", method= RequestMethod.GET)
    public AdminUserInfo getCurrentUser(HttpServletRequest req) {

        // FIXME Hack temporaire le temps de basculer l'ensemble du modele
        io.cfp.model.User newUser = authUtils.getAuthUser(req);
        User user = null;
        if (newUser != null) {
            user = new User(newUser);
        }

        if (user == null) {
            return new AdminUserInfo("./", null);
        }

        AdminUserInfo infos = new AdminUserInfo("./logout", user.getEmail());
        user.addRoles(roles.findByUserIdAndEventId(user.getId(), Event.current()).stream().map(r -> r.getName()).collect(Collectors.toList()));
        for (String role : user.getRoles()) {
        	if (Role.ADMIN.equals(role)) {
        		infos.setAdmin(true);
        	}
        	if (Role.OWNER.equals(role)) {
        		infos.setOwner(true);
        	}
            if (Role.REVIEWER.equals(role)) {
                infos.setReviewer(true);
            }
         }

        return infos;
    }
}
