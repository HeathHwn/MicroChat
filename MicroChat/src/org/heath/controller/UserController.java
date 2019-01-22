package org.heath.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.ibatis.annotations.Param;
import org.heath.entity.Friend;
import org.heath.entity.User;
import org.heath.entity.UserInfo;
import org.heath.service.IFriendService;
import org.heath.service.IUserService;
import org.heath.utils.Common;
import org.heath.utils.HttpClient;
import org.heath.utils.RandomCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@ResponseBody
@RequestMapping("user")
public class UserController {

	@Autowired
	@Qualifier("userServiceImpl")
	IUserService iUserService;

	public void setiUserService(IUserService iUserService) {
		this.iUserService = iUserService;
	}

	@Autowired
	@Qualifier("friendServiceImpl")
	IFriendService iFriendService;

	public void setiFriendService(IFriendService iFriendService) {
		this.iFriendService = iFriendService;
	}

	// ��¼
	@RequestMapping("login.action")
	private Map<String, Object> login(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
			User user1 = iUserService.login(user);
			if (user1 != null) {
				if (user1.getState().equals("0")) {
					long cTime = System.currentTimeMillis();
					if (Long.parseLong(user1.getProhibitionTime()) > cTime) {
						returnMap.put("code", "808");
						returnMap.put("msg", "����˻�������ˣ������" + Common.stampToDate(user1.getProhibitionTime()));
					} else {
						int num = iUserService.resetProhibition(user1.getAccount());
						if (num > 0) {
							UserInfo userInfo = iUserService.queryMyInfo(user1.getAccount());
							List<Friend> friends = iFriendService.queryAllFriends(user1.getAccount());
							returnMap.put("code", "200");
							returnMap.put("userInfo", userInfo);
							returnMap.put("account", user1.getAccount());
							returnMap.put("token", user1.getToken());
							returnMap.put("friends", JSONArray.fromObject(friends));
							returnMap.put("msg", "��¼�ɹ�");
						} else {
							returnMap.put("code", "404");
							returnMap.put("msg", "�����쳣");
						}
					}
				} else {
					UserInfo userInfo = iUserService.queryMyInfo(user1.getAccount());
					List<Friend> friends = iFriendService.queryAllFriends(user1.getAccount());
					returnMap.put("code", "200");
					returnMap.put("userInfo", userInfo);
					returnMap.put("account", user1.getAccount());
					returnMap.put("token", user1.getToken());
					returnMap.put("friends", JSONArray.fromObject(friends));
					returnMap.put("msg", "��¼�ɹ�");
				}
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "�û������������");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}

	// �����˻�
	@RequestMapping("create.action")
	private Map<String, Object> register(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
			int count = iUserService.checkRegister(user.getBindMobile());
			int count1 = 1;
			if (count >= 1) {
				returnMap.put("code", "414");
				returnMap.put("msg", "���ֻ����ѱ�ע��");
			} else {
				while (count1 == 1) {
					String account = Common.getRandomCode();
					user.setAccount(account);
					count1 = iUserService.checkAccount(user.getAccount());
					if (count1 == 0) {
						break;
					}
				}
				String token = RandomCode.getRandomCode();
				JSONObject httpParameter = new JSONObject();
				JSONObject parameter = new JSONObject();
				parameter.put("accid", user.getAccount());
				parameter.put("name", user.getAccount());
				parameter.put("token", token);
				httpParameter.put("url", "https://api.netease.im/nimserver/user/create.action");
				httpParameter.put("parameter", parameter);
				JSONObject returnObj = HttpClient.httpClient(httpParameter);
				if (returnObj.get("code").equals(200)) {
					user.setToken(token);
					iUserService.register(user);
					iUserService.addUserinfo(user.getAccount());
					returnMap.put("code", "200");
					returnMap.put("msg", "ע��ɹ�");
				} else if (returnObj.get("code").equals(414)) {
					returnMap.put("code", "414");
					returnMap.put("msg", "���ֻ����ѱ�ע��");
				} else {
					returnMap.put("code", "1000");
					returnMap.put("msg", "ע��ʧ�ܣ�������ע��");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	@RequestMapping("updateMyInfo.action")
	private Map<String, Object> updateMyInfo(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			UserInfo userInfo = (UserInfo) JSONObject.toBean(JSONObject.fromObject(parameterData), UserInfo.class);
			JSONObject httpParameter = new JSONObject();
			JSONObject parameter = new JSONObject();
			System.out.println(userInfo);
			parameter.put("accid", userInfo.getAccount());
			if (userInfo.getNickname() != null) {
				parameter.put("name", userInfo.getNickname());
			}
			if (userInfo.getIcon() != null) {
				parameter.put("icon", userInfo.getIcon());
			}
			if (userInfo.getSign() != null) {
				parameter.put("sign", userInfo.getSign());
			}
			if (userInfo.getEmail() != null) {
				parameter.put("email", userInfo.getEmail());
			}
			if (userInfo.getBirth() != null) {
				parameter.put("birth", userInfo.getBirth());
			}
			if (userInfo.getMobile() != null) {
				parameter.put("mobile", userInfo.getMobile());
			}
			if (userInfo.getGender() != null) {
				parameter.put("gender", userInfo.getGender());
			}
			if (userInfo.getEx() != null) {
				parameter.put("ex", userInfo.getEx());
			}
			httpParameter.put("url", "https://api.netease.im/nimserver/user/update.action");
			httpParameter.put("parameter", parameter);
			JSONObject returnObj = HttpClient.httpClient(httpParameter);
			if (returnObj.get("code").equals(200)) {
				int result = iUserService.modifyMyInfo(userInfo);
				if (result > 0) {
					returnMap.put("code", "200");
					returnMap.put("msg", "�޸����ϳɹ�");
				} else {
					returnMap.put("code", "414");
					returnMap.put("msg", "�޸�����ʧ�ܣ��������޸�");
				}
			} else if (returnObj.get("code").equals(414)) {
				returnMap.put("code", "414");
				returnMap.put("msg", "�޸�����ʧ�ܣ��������޸�");
			} else {
				returnMap.put("code", "1000");
				returnMap.put("msg", "�޸�����ʧ�ܣ��������޸�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// �޸�����
	@RequestMapping("modifyPassword.action")
	private Map<String, Object> modifyPassword(@Param("parameterData") String parameterData) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			JSONObject userObj = JSONObject.fromObject(parameterData);
			Map<String, Object> user = new HashMap<String, Object>();
			user.put("account", userObj.getString("account"));
			user.put("password", userObj.getString("password"));
			user.put("newPassword", userObj.getString("newPassword"));
			int num = iUserService.modifyPassword(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "�޸ĳɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "�û�������������������޸�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}

	@RequestMapping("getUinfos.action")
	private Map<String, Object> getUinfos(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		JSONObject accountsObject = JSONObject.fromObject(parameterData);
		JSONObject httpParameter = new JSONObject();
		JSONObject parameter = new JSONObject();
		parameter.put("accids", accountsObject.getJSONArray("accounts"));
		httpParameter.put("url", "https://api.netease.im/nimserver/user/getUinfos.action");
		httpParameter.put("parameter", parameter);
		JSONObject returnObj = HttpClient.httpClient(httpParameter);
		if (returnObj.get("code").equals(200)) {
			returnMap.put("code", "200");
			returnMap.put("uinfos", returnObj.getJSONArray("uinfos"));
		} else if (returnObj.get("code").equals(414)) {
			returnMap.put("code", "414");
			returnMap.put("msg", "���˻�������");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// �����˺Ų�ѯ�û���Ϣ
	@RequestMapping("queryUsersInfo.action")
	private Map<String, Object> queryUsersInfo(@Param("parameterData") String parameterData) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			JSONObject accountsObject = JSONObject.fromObject(parameterData);
			JSONArray arrayAccounts = accountsObject.getJSONArray("accounts");
			List<String> accounts = new ArrayList<String>();
			if (arrayAccounts.size() == 0) {
				returnMap.put("code", "400");
				returnMap.put("msg", "������");
				return returnMap;
			}
			for (Object account : arrayAccounts) {
				accounts.add(account.toString());
			}
			List<UserInfo> userInfos = iUserService.queryUsersInfo(accounts);
			returnMap.put("code", "200");
			returnMap.put("userInfos", userInfos);
			returnMap.put("msg", "����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}

	// �����˺Ų�ѯ�û���Ϣ
	@RequestMapping("queryMyInfo.action")
	private Map<String, Object> queryMyInfo(@Param("parameterData") String parameterData) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
			UserInfo userInfo = iUserService.queryMyInfo(user.getAccount());
			returnMap.put("code", "200");
			returnMap.put("userInfo", userInfo);
			returnMap.put("msg", "����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}

	// ��������
	@RequestMapping("resetPassword.action")
	private Map<String, Object> resetPassword(@Param("parameterData") String parameterData) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
			int num = iUserService.resetPassword(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "���óɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "����ʧ�ܣ��������޸�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}

	// �˺ŷ��
	@RequestMapping("accountBan.action")
	private Map<String, Object> accountBan(@Param("account") String account, @Param("hour") int hour,
			@Param("day") int day) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			if (day <= 0) {
				day = 0;
			}
			if (hour <= 0) {
				hour = 0;
			} else if (hour >= 24) {
				hour = 24;
			}
			long time = System.currentTimeMillis();
			time = time + day * 24 * 60 * 60 * 1000 + hour * 60 * 60 * 1000;
			User user = new User();
			user.setAccount(account);
			user.setState("0");
			user.setProhibitionTime(time + "");
			int num = iUserService.accountBan(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "����ɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "���ʧ�ܣ���ȷ�Ϸ���˺�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}
	// �˺Ž��
	@RequestMapping("accountDecapsulation.action")
	private Map<String, Object> accountDecapsulation(@Param("account") String account) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = new User();
			user.setAccount(account);
			user.setState("1");
			user.setProhibitionTime("");
			int num = iUserService.accountBan(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "���ɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "���ʧ�ܣ���ȷ�Ͻ���˺�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}
	// �˺���������
	@RequestMapping("accountResetPassword.action")
	private Map<String, Object> accountResetPassword(@Param("account") String account) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = new User();
			String password = Common.getRandomCode();
			user.setBindMobile(account);
			user.setPassword(password);
			int num = iUserService.resetPassword(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "���óɹ�");
				returnMap.put("password", password);
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "����ʧ�ܣ�����������");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}
	// ��ѯ��ǰ��ÿһ���·�
	@RequestMapping("queryEveryMonthRegisterNum.action")
	private Map<String, Object> queryEveryMonthRegisterNum() {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Calendar cale = null;  
	        cale = Calendar.getInstance();  
	        int year = cale.get(Calendar.YEAR);
			List<Map<String, Object>> list = iUserService.queryEveryMonthRegisterNum(year);
			returnMap.put("code", "200");
			returnMap.put("msg", "��ѯ�ɹ�");
			returnMap.put("list", list);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}
	// ��ѯ�û���Ϣ
	@RequestMapping("queryUserinfo.action")
	private Map<String, Object> queryUserinfo(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("start", (pageNum - 1) * pageSize);
			map.put("pageSize", pageSize);
			List<Map<String, Object>> list = iUserService.queryUserinfo(map);
			int count = iUserService.queryUserNum();
			returnMap.put("code", "200");
			returnMap.put("msg", "��ѯ�ɹ�");
			returnMap.put("list", list);
			returnMap.put("count", count);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}
	// ����Ա��¼
	@RequestMapping("adminLogin.action")
	private Map<String, Object> adminLogin(@Param("account") String account,@Param("password") String password) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			if(account.equals("admin") && password.equals("admin")){
				returnMap.put("code", "200");
				returnMap.put("msg", "��¼�ɹ�");
			}else{
				returnMap.put("code", "404");
				returnMap.put("msg", "�˺Ż��������");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		return returnMap;
	}

}
