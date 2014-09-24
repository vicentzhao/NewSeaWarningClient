package xu.ye.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import xu.ye.bean.ContactBean;
import xu.ye.bean.PhoneLog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

public class ContactInfoService {

	private static final String TAG = "ConcactInfoService";
	private static ContactInfoService instance;
	Context context;

	public static final int COL_ID = 0;
	public static final int COL_NAME = 1;
	public static final int COL_HAS_PHONE = 2;

	final String[] selectCol = new String[] { ContactsContract.Contacts._ID,
			ContactsContract.Contacts.DISPLAY_NAME,
			ContactsContract.Contacts.HAS_PHONE_NUMBER

	};
	final String[] selPhoneCols = new String[] {
			ContactsContract.CommonDataKinds.Phone._ID,
			ContactsContract.CommonDataKinds.Phone.NUMBER,
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Phone.TYPE,
			ContactsContract.CommonDataKinds.Phone.LABEL,
			ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };

	public ContactInfoService(Context context) {
		this.context = context;
	}

	public static ContactInfoService getInstance(Context context) {
		if (null == instance) {
			instance = new ContactInfoService(context);
		}
		return instance;
	}

	public ArrayList<PhoneLog> getCallLogs() {

		ArrayList<PhoneLog> lists = null;
		final String[] selCallLogs = new String[] { CallLog.Calls.CACHED_NAME,
				CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE };
		Cursor cur = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, selCallLogs, null, null,
				CallLog.Calls.DEFAULT_SORT_ORDER);
		if (null == cur) {
			return lists;
		}
		lists = new ArrayList<PhoneLog>();
		PhoneLog p;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = new Date();
		while (cur.moveToNext()) {
			p = new PhoneLog();
			p.cachedName = cur.getString(0);
			p.number = cur.getString(1);
			p.type = cur.getShort(2);
			date.setTime(cur.getLong(3));
			p.callData = sdf.format(date);
			lists.add(p);
		}
		cur.close();
		return lists;
	}

	/**
	 * 根据号码获取联系人
	 * 
	 * @param name
	 * @return
	 */
	/*public Cursor getContact(String name) {

		final String select = ContactsContract.CommonDataKinds.Phone.NUMBER
				+ " like '" + name + "%'";
		final Cursor cur = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				selPhoneCols, select, null, "sort_key_alt");
		return cur;
	}*/
    public String getContactNameFromPhoneBook(Context context, String phoneNum) {  
    	
    	if(null==phoneNum||"".equals(phoneNum)){
    		return "";
    	}
    	
        String contactName = "";  
        
        ContentResolver cr = context.getContentResolver();  
        Cursor pCur = cr.query(  
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,  
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",  
                new String[] { phoneNum }, null);  
        if (pCur.moveToFirst()) {  
            contactName = pCur  
                    .getString(pCur  
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));  
            pCur.close();  
        }else{
        	if(phoneNum.startsWith("+86")){
        		String substring = phoneNum.substring(3, phoneNum.length());
        		System.out.println(substring);
        		return getContactNameFromPhoneBook(context,substring);
        	}
        }
        return contactName;  
    }  

	
//    private String getPeopleNameFromPerson(String address){  
//        if(address == null || address == ""){  
//            return null;  
//        }  
//          
//        String strPerson = "null";  
//        String[] projection = new String[] {Phone.DISPLAY_NAME, Phone.NUMBER};  
//          
//        Uri uri_Person = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, address);  // address 手机号过滤  
//        Cursor cursor = getContentResolver().query(uri_Person, projection, null, null, null);  
//          
//        if(cursor.moveToFirst()){  
//            int index_PeopleName = cursor.getColumnIndex(Phone.DISPLAY_NAME);  
//            String strPeopleName = cursor.getString(index_PeopleName);  
//            strPerson = strPeopleName;  
//        }  
//        else{  
//            strPerson = address;  
//        }  
//        cursor.close();  
//        cursor=null;  
//        return strPerson;  
//    } 
	/**
	 * 获取全部联系人电话
	 * 
	 * @return
	 */
	public ArrayList<ContactBean> getContact() {
		ArrayList<ContactBean> peoples = null;
		final String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
				+ Contacts.HAS_PHONE_NUMBER + "=1) AND ("
				+ Contacts.DISPLAY_NAME + " != '' ))";
		Cursor cur = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				selectCol,
				select,
				null,
				ContactsContract.Contacts.DISPLAY_NAME
						+ " COLLATE LOCALIZED ASC");
		if (null == cur) {
			return peoples;
		}
		peoples = new ArrayList<ContactBean>();
		if (cur.moveToFirst()) {
			do {
				ContactBean cb = new ContactBean();
				// 联系人的ID
				String contactId = cur.getString(COL_ID);
				String name = cur.getString(COL_NAME);
				// 根据姓名 查看有多少个联系号码
				int numberCount = cur.getInt(COL_HAS_PHONE);
				if (numberCount > 0) {

					Cursor phone = context.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							selPhoneCols,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ "=" + contactId, null, null);

					if (null != phone && phone.moveToFirst()) {
						do {
							String phoneNumber = phone
									.getString(phone
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							short phoneType = phone
									.getShort(phone
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
							if (phoneType == 1) {
//								cb.setContactHomePhone(phoneNumber);
								cb.setPhoneNum(phoneNumber);
							} else {
								cb.setPhoneNum(phoneNumber);
							}
							if (!name.equals(phoneNumber)) {
								cb.setDisplayName(name);
							}
							cb.setContactId(Integer.parseInt(contactId));
						} while (phone.moveToNext());
					}
				}
				peoples.add(cb);
			} while (cur.moveToNext());
		}

		return peoples;
	}
}
