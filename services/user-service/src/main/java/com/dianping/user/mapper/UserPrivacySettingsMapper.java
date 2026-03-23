package com.dianping.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dianping.user.entity.UserPrivacySettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户隐私设置Mapper
 */
@Mapper
public interface UserPrivacySettingsMapper extends BaseMapper<UserPrivacySettings> {
    
    @Select("SELECT * FROM dp_user_privacy_settings WHERE user_id = #{userId}")
    UserPrivacySettings selectByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE dp_user_privacy_settings SET " +
            "show_posts = #{showPosts}, " +
            "show_following = #{showFollowing}, " +
            "show_followers = #{showFollowers}, " +
            "show_phone = #{showPhone}, " +
            "show_email = #{showEmail} " +
            "WHERE user_id = #{userId}")
    int updateByUserId(UserPrivacySettings settings);
}
