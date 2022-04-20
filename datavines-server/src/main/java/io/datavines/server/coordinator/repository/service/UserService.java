package io.datavines.server.coordinator.repository.service;

import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.api.dto.user.UserLogin;
import io.datavines.server.coordinator.api.dto.user.UserRegister;
import io.datavines.server.coordinator.api.dto.user.UserResetPassword;
import io.datavines.server.coordinator.api.dto.user.UserUpdate;
import io.datavines.server.coordinator.repository.entity.User;

public interface UserService {

    User getByUsername(String username);

    User userLogin(UserLogin userLogin) throws DataVinesException;

    User register(UserRegister userRegister) throws DataVinesException;

    Boolean updateUserInfo(UserUpdate userUpdate);

    Boolean resetPassword(UserResetPassword userResetPassword);
}
