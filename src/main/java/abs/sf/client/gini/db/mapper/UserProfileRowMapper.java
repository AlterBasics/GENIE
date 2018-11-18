package abs.sf.client.gini.db.mapper;

import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.UserProfileData;

public class UserProfileRowMapper implements RowMapper<UserProfileData> {
    @Override
    public UserProfileData map(Cursor cursor) throws SQLException {
        UserProfileData userProfileData = new UserProfileData();

        try {

            userProfileData.setJabberId(new JID(cursor.getString(0)));

        } catch (Exception e) {
            //swallow exception
        }

        userProfileData.setFirstName(cursor.getString(1));
        userProfileData.setMiddleName(cursor.getString(2));
        userProfileData.setLastName(cursor.getString(3));
        userProfileData.setNickName(cursor.getString(4));
        userProfileData.setEmail(cursor.getString(5));
        userProfileData.setPhone(cursor.getString(6));
        userProfileData.setGender(cursor.getString(7));
        userProfileData.setBday(cursor.getString(8));

        UserProfileData.Address address = userProfileData.new Address();
        userProfileData.setAddress(address);

        address.setHome(cursor.getString(9));
        address.setStreet(cursor.getString(10));
        address.setLocality(cursor.getString(11));
        address.setCity(cursor.getString(12));
        address.setState(cursor.getString(13));
        address.setCountry(cursor.getString(14));
        address.setPcode(cursor.getString(15));

        userProfileData.setDescription(cursor.getString(16));
        
        return userProfileData;
    }
}
