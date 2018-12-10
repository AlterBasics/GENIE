package abs.sf.client.gini.db.mapper;


import java.sql.ResultSet;
import java.sql.SQLException;

import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.UserProfileData;

public class UserProfileRowMapper implements RowMapper<UserProfileData> {
    @Override
    public UserProfileData map(ResultSet rs) throws SQLException {
        UserProfileData userProfileData = new UserProfileData();

        try {

            userProfileData.setJabberId(new JID(rs.getString(1)));

        } catch (Exception e) {
            //swallow exception
        }

        userProfileData.setFirstName(rs.getString(2));
        userProfileData.setMiddleName(rs.getString(3));
        userProfileData.setLastName(rs.getString(4));
        userProfileData.setNickName(rs.getString(5));
        userProfileData.setEmail(rs.getString(6));
        userProfileData.setPhone(rs.getString(7));
        userProfileData.setGender(rs.getString(8));
        userProfileData.setBday(rs.getString(9));

        UserProfileData.Address address = userProfileData.new Address();
        userProfileData.setAddress(address);

        address.setHome(rs.getString(10));
        address.setStreet(rs.getString(11));
        address.setLocality(rs.getString(12));
        address.setCity(rs.getString(13));
        address.setState(rs.getString(14));
        address.setCountry(rs.getString(15));
        address.setPcode(rs.getString(16));

        userProfileData.setDescription(rs.getString(17));
        
        return userProfileData;
    }
}
