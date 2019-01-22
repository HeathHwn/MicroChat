package org.heath.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.ibatis.annotations.Param;
import org.heath.entity.Team;
import org.heath.entity.TeamMember;
import org.heath.entity.TeamRelationship;
import org.heath.service.ITeamService;
import org.heath.utils.Common;
import org.heath.utils.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@ResponseBody
@RequestMapping("team")
public class TeamController {

	@Autowired
	@Qualifier("teamServiceImpl")
	ITeamService iTeamService;

	public void setiTeamService(ITeamService iTeamService) {
		this.iTeamService = iTeamService;
	}

	// ����Ⱥ
	@RequestMapping("create.action")
	private Map<String, Object> create(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Team team = (Team) JSONObject.toBean(JSONObject.fromObject(parameterData), Team.class);
			JSONObject httpParameter = new JSONObject();
			JSONObject parameter = new JSONObject();
			parameter.put("tname", team.getTname());
			parameter.put("owner", team.getOwner());
			parameter.put("members", team.getMembers().replace("\"", ""));
			parameter.put("announcement", team.getAnnouncement());
			parameter.put("intro", team.getIntro());
			parameter.put("msg", team.getMsg());
			parameter.put("magree", team.getMagree());
			parameter.put("joinmode", team.getJoinmode());
			parameter.put("custom", team.getCustom());
			parameter.put("icon", team.getIcon());
			parameter.put("beinvitemode", team.getBeinvitemode());
			parameter.put("invitemode", team.getInvitemode());
			parameter.put("uptinfomode", team.getUptinfomode());
			parameter.put("upcustommode", team.getUpcustommode());
			parameter.put("teamMemberLimit", team.getTeamMemberLimit());
			httpParameter.put("url", "https://api.netease.im/nimserver/team/create.action");
			httpParameter.put("parameter", parameter);
			JSONObject returnObj = HttpClient.httpClient(httpParameter);
			System.out.println(httpParameter.toString());
			if (returnObj.get("code").equals(200)) {
				team.setTid(returnObj.get("tid").toString());
				JSONArray memberArray = JSONArray.fromObject(team.getMembers().replace("\"", ""));
				memberArray.add(team.getOwner());
				team.setMembers(memberArray.toString());
				int num = iTeamService.addTeam(team);
				List<TeamMember> teamMembers = new ArrayList<TeamMember>();
				TeamMember teamMember = new TeamMember(team.getOwner(), "", new Date().getTime() + "", "",
						returnObj.get("tid").toString(), "Owner", "1", "0");
				teamMembers.add(teamMember);
				iTeamService.addTeamMember(teamMembers);
				if (num > 0) {
					returnMap.put("code", "200");
					returnMap.put("msg", "����Ⱥ�ɹ�");
				} else {
					JSONObject httpParameter1 = new JSONObject();
					JSONObject parameter1 = new JSONObject();
					parameter1.put("tid", returnObj.get("tid").toString());
					parameter1.put("owner", team.getOwner());
					httpParameter1.put("url", "https://api.netease.im/nimserver/team/remove.action");
					httpParameter1.put("parameter", parameter1);
					HttpClient.httpClient(httpParameter1);
					returnMap.put("code", "414");
					returnMap.put("msg", "����Ⱥʧ�ܣ������´���");
				}
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "����Ⱥʧ�ܣ������´���");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ������Ⱥ
	@RequestMapping("applyJoinTeam.action")
	private Map<String, Object> applyJoinTeam(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		int num = 0;
		try {
			TeamRelationship teamRelationship = (TeamRelationship) JSONObject
					.toBean(JSONObject.fromObject(parameterData), TeamRelationship.class);
			if (iTeamService.queryTeamRelationshipByTidAndMore(teamRelationship) > 0) {
				num = iTeamService.modifyApplyJoinTeam(teamRelationship);
			} else {
				num = iTeamService.applyJoinTeam(teamRelationship);
			}
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "������Ⱥ�ɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "������Ⱥʧ�ܣ�������������Ⱥ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// �޸�Ⱥ��Ա�ǳ�
	@RequestMapping("modifyTeamMemberNick.action")
	private Map<String, Object> modifyTeamMemberNick(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TeamMember teamMember = (TeamMember) JSONObject.toBean(JSONObject.fromObject(parameterData),
					TeamMember.class);
			int num = iTeamService.modifyTeamMemberByTidAndAccount(teamMember);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "�޸�Ⱥ��Ա���ϳɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "�޸�Ⱥ��Ա����ʧ�ܣ��������޸�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// �Ƴ�Ⱥ��Ա
	@RequestMapping("removeMember.action")
	private Map<String, Object> removeMember(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, String> map = new HashMap<String, String>();
			String tid = JSONObject.fromObject(parameterData).getString("tid");
			String account = JSONObject.fromObject(parameterData).getString("account");
			String membersDB = iTeamService.queryTeamMemberByTid(tid);
			JSONArray membersDBArray = JSONArray.fromObject(membersDB.replace("\"", ""));
			membersDBArray.remove(Long.parseLong(account));
			map.put("tid", tid);
			map.put("members", membersDBArray.toString());
			int num = iTeamService.modifyTeamMemberByTid(map);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "�Ƴ���Ⱥ�ɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "�Ƴ���Ⱥʧ�ܣ��������Ƴ�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ɾ�����ݿ�Ⱥ��Ա
	@RequestMapping("delTeamMemberByTidAndAccount.action")
	private Map<String, Object> delTeamMemberByTidAndAccount(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("tid", JSONObject.fromObject(parameterData).getString("tid"));
			map.put("account", JSONObject.fromObject(parameterData).getString("account"));
			int num = iTeamService.delTeamMemberByTidAndAccount(map);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "�Ƴ���Ⱥ�ɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "�Ƴ���Ⱥʧ�ܣ��������Ƴ�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// �޸�Ⱥ����
	@RequestMapping("modifyTeamByTid.action")
	private Map<String, Object> modifyTeamByTid(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Team team = (Team) JSONObject.toBean(JSONObject.fromObject(parameterData), Team.class);
			JSONObject httpParameter = new JSONObject();
			JSONObject parameter = new JSONObject();
			parameter.put("owner", team.getOwner());
			parameter.put("tid", team.getTid());
			if (team.getTname() != null) {
				parameter.put("tname", team.getTname());
			}
			if (team.getAnnouncement() != null) {
				parameter.put("announcement", team.getAnnouncement());
			}
			if (team.getIntro() != null) {
				parameter.put("intro", team.getIntro());
			}
			if (team.getJoinmode() != null) {
				parameter.put("joinmode", team.getJoinmode());
			}
			if (team.getCustom() != null) {
				parameter.put("custom", team.getCustom());
			}
			if (team.getIcon() != null) {
				parameter.put("icon", team.getIcon());
			}
			if (team.getBeinvitemode() != null) {
				parameter.put("beinvitemode", team.getBeinvitemode());
			}
			if (team.getInvitemode() != null) {
				parameter.put("invitemode", team.getInvitemode());
			}
			if (team.getUptinfomode() != null) {
				parameter.put("uptinfomode", team.getUptinfomode());
			}
			if (team.getUpcustommode() != null) {
				parameter.put("upcustommode", team.getUpcustommode());
			}
			if (team.getTeamMemberLimit() != null) {
				parameter.put("teamMemberLimit", team.getTeamMemberLimit());
			}
			httpParameter.put("url", "https://api.netease.im/nimserver/team/update.action");
			httpParameter.put("parameter", parameter);
			JSONObject returnObj = HttpClient.httpClient(httpParameter);
			if (returnObj.get("code").equals(200)) {
				int num = iTeamService.modifyTeamByTid(team);
				if (num > 0) {
					returnMap.put("code", "200");
					returnMap.put("msg", "�޸�Ⱥ���ϳɹ�");
				} else {
					returnMap.put("code", "414");
					returnMap.put("msg", "�޸�Ⱥ����ʧ�ܣ��������޸�");
				}
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "�޸�Ⱥ����ʧ�ܣ��������޸�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ɾ�����ݿ�Ⱥ��Ա
	@RequestMapping("queryTeamInfoByTid.action")
	private Map<String, Object> queryTeamInfoByTid(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String tid = JSONObject.fromObject(parameterData).getString("tid");
			Team team = iTeamService.queryTeamInfoByTid(tid);
			returnMap.put("code", "200");
			returnMap.put("team", team);
			returnMap.put("msg", "��ȡ�ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ����Ⱥ
	@RequestMapping("queryTeams.action")
	private Map<String, Object> queryTeams(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String text = JSONObject.fromObject(parameterData).getString("text");
			List<Team> teams = iTeamService.queryTeams(text);
			returnMap.put("code", "200");
			returnMap.put("teams", teams);
			returnMap.put("msg", "����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ��������Ⱥ
	@RequestMapping("invitation.action")
	private Map<String, Object> invitation(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Team team = (Team) JSONObject.toBean(JSONObject.fromObject(parameterData).getJSONObject("team"),
					Team.class);
			String type = JSONObject.fromObject(parameterData).getString("type");
			JSONObject httpParameter = new JSONObject();
			JSONObject parameter = new JSONObject();
			List<TeamRelationship> teamRelationships = new ArrayList<TeamRelationship>();
			List<TeamMember> teamMembers = new ArrayList<TeamMember>();
			JSONArray members = JSONArray.fromObject(team.getMembers().replace("\"", ""));
			String tid = team.getTid();
			String owner = team.getOwner();
			String msg = team.getMsg();
			parameter.put("tid", tid);
			parameter.put("owner", owner);
			parameter.put("members", team.getMembers().replace("\"", ""));
			parameter.put("msg", msg);
			parameter.put("magree", team.getMagree());
			httpParameter.put("url", "https://api.netease.im/nimserver/team/add.action");
			httpParameter.put("parameter", parameter);
			JSONObject returnObj = HttpClient.httpClient(httpParameter);
			System.out.println(httpParameter.toString());
			String membersDB = iTeamService.queryTeamMemberByTid(tid);
			JSONArray membersDBArray = JSONArray.fromObject(membersDB.replace("\"", ""));
			if (returnObj.get("code").equals(200)) {
				for (int i = 0; i < members.size(); i++) {
					TeamRelationship teamRelationship = new TeamRelationship(tid, owner, members.get(i).toString(), msg,
							"1", type);
					teamRelationships.add(teamRelationship);
					TeamMember teamMember = new TeamMember(members.get(i).toString(), "", new Date().getTime() + "", "",
							tid, "Normal", "1", "0");
					teamMembers.add(teamMember);
				}
				int num = iTeamService.invitation(teamRelationships);
				iTeamService.addTeamMember(teamMembers);
				Map<String, String> map = new HashMap<String, String>();
				map.put("tid", tid);
				map.put("members", Common.joinJSONArray(members, membersDBArray).toString());
				iTeamService.modifyTeamMemberByTid(map);
				if (num > 0) {
					returnMap.put("code", "200");
					returnMap.put("msg", "����ɹ�");
				} else {
					returnMap.put("code", "414");
					returnMap.put("msg", "����ʧ�ܣ�����������");
				}
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "����ʧ�ܣ�����������");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ����Ⱥ֪ͨδ������
	@RequestMapping("queryTeamRelationshipNoticeNumByAccount.action")
	private Map<String, Object> queryTeamRelationshipNoticeNumByAccount(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String account = JSONObject.fromObject(parameterData).getString("account");
			int num = iTeamService.queryTeamRelationshipNoticeNumByAccount(account);
			returnMap.put("code", "200");
			returnMap.put("num", num);
			returnMap.put("msg", "����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ����Ⱥ֪ͨδ������
	@RequestMapping("queryTeamRelationshipNoticeByAccount.action")
	private Map<String, Object> queryTeamRelationshipNoticeByAccount(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String account = JSONObject.fromObject(parameterData).getString("account");
			List<TeamRelationship> teamRelationships = iTeamService.queryTeamRelationshipNoticeByAccount(account);
			returnMap.put("code", "200");
			returnMap.put("teamRelationships", teamRelationships);
			returnMap.put("msg", "����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// �޸�Ⱥ��֪ͨ״̬
	@RequestMapping("modifyTeamRelationshipById.action")
	private Map<String, Object> modifyTeamRelationshipById(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TeamRelationship teamRelationship = (TeamRelationship) JSONObject
					.toBean(JSONObject.fromObject(parameterData), TeamRelationship.class);
			if (teamRelationship.getState() != null) {
				if (teamRelationship.getState().equals("1")) {
					String tid = teamRelationship.getTid();
					String account = teamRelationship.getBeinviter();
					List<TeamMember> teamMembers = new ArrayList<TeamMember>();
					TeamMember teamMember = new TeamMember(account, "", new Date().getTime() + "", "", tid, "Normal",
							"1", "0");
					teamMembers.add(teamMember);
					iTeamService.addTeamMember(teamMembers);
					JSONArray members = new JSONArray();
					members.add(account);
					String membersDB = iTeamService.queryTeamMemberByTid(tid);
					JSONArray membersDBArray = JSONArray.fromObject(membersDB.replace("\"", ""));
					Map<String, String> map = new HashMap<String, String>();
					map.put("tid", tid);
					map.put("members", Common.joinJSONArray(members, membersDBArray).toString());
					iTeamService.modifyTeamMemberByTid(map);
				}
			}
			int num = iTeamService.modifyTeamRelationshipById(teamRelationship);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "����ɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "����ʧ��");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ��ɢȺ
	@RequestMapping("dissolution.action")
	private Map<String, Object> dissolution(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Team team = (Team) JSONObject.toBean(JSONObject.fromObject(parameterData), Team.class);
			JSONObject httpParameter = new JSONObject();
			JSONObject parameter = new JSONObject();
			String tid = team.getTid();
			String owner = team.getOwner();
			parameter.put("tid", tid);
			parameter.put("owner", owner);
			httpParameter.put("url", "https://api.netease.im/nimserver/team/remove.action");
			httpParameter.put("parameter", parameter);
			JSONObject returnObj = HttpClient.httpClient(httpParameter);
			System.out.println(httpParameter.toString());
			if (returnObj.get("code").equals(200)) {
				iTeamService.delTeamByTid(tid);
				iTeamService.delTeamMemberByTid(tid);
				iTeamService.delTeamRelationshiByTid(tid);
				returnMap.put("code", "200");
				returnMap.put("msg", "��ɢ�ɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "����ʧ�ܣ�����������");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ����
	@RequestMapping("mute.action")
	private Map<String, Object> mute(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			JSONObject team = JSONObject.fromObject(parameterData);
			JSONObject httpParameter = new JSONObject();
			JSONObject parameter = new JSONObject();
			String tid = team.getString("tid");
			String owner = team.getString("owner");
			int muteType = team.getInt("muteType");
			parameter.put("tid", tid);
			parameter.put("owner", owner);
			parameter.put("muteType", muteType);
			httpParameter.put("url", "https://api.netease.im/nimserver/team/muteTlistAll.action");
			httpParameter.put("parameter", parameter);
			JSONObject returnObj = HttpClient.httpClient(httpParameter);
			System.out.println(httpParameter.toString());
			if (returnObj.get("code").equals(200)) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("tid", tid);
				if (muteType == 0) {
					map.put("isMute", "0");
				} else if (muteType == 1) {
					map.put("isMute", "1");
					map.put("type", "Normal");
				} else {
					map.put("isMute", "1");
				}
				iTeamService.modifyTeamMember(map);
				returnMap.put("code", "200");
				returnMap.put("msg", "����ɹ�");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "����ʧ�ܣ�����������");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// ����Ⱥ��Ա
	@RequestMapping("muteMember.action")
	private Map<String, Object> muteMember(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			JSONObject team = JSONObject.fromObject(parameterData);
			JSONObject httpParameter = new JSONObject();
			JSONObject parameter = new JSONObject();
			String tid = team.getString("tid");
			String accid = team.getString("account");
			String owner = team.getString("owner");
			int mute = team.getInt("mute");
			parameter.put("tid", tid);
			parameter.put("owner", owner);
			parameter.put("accid", accid);
			parameter.put("mute", mute);
			httpParameter.put("url", "https://api.netease.im/nimserver/team/muteTlist.action");
			httpParameter.put("parameter", parameter);
			JSONObject returnObj = HttpClient.httpClient(httpParameter);
			System.out.println(httpParameter.toString());
			if (returnObj.get("code").equals(200)) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("tid", tid);
				map.put("account", accid);
				map.put("isMute", mute + "");
				iTeamService.modifyTeamMember(map);
				returnMap.put("code", "200");
				if(mute==0){
					returnMap.put("msg", "������Գɹ�");
				}else{
					returnMap.put("msg", "���Գɹ�");
				}
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "���Բ���ʧ�ܣ������²���");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "�����쳣");
		}
		System.out.println(returnMap);
		return returnMap;
	}

}
