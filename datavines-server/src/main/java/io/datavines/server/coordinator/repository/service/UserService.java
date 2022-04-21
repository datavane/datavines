package io.datavines.server.coordinator.repository.service;

import io.datavines.common.dto.user.*;
import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.repository.entity.User;

public interface UserService {

    User getByUsername(String username);

    UserLoginResult login(UserLogin userLogin) throws DataVinesException;

    UserBaseInfo register(UserRegister userRegister) throws DataVinesException;

    Boolean updateUserInfo(UserUpdate userUpdate);

    Boolean resetPassword(UserResetPassword userResetPassword);
}
