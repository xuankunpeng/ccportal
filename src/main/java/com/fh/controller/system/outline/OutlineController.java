package com.fh.controller.system.outline;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fh.controller.base.BaseController;
import com.fh.entity.Page;
import com.fh.util.AppUtil;
import com.fh.util.CallResult;
import com.fh.util.DataAccessUtils;
import com.fh.util.GsonUtils;
import com.fh.util.ObjectExcelView;
import com.fh.util.PageData;
import com.fh.util.Jurisdiction;
import com.fh.util.Tools;
import com.fh.service.system.outline.OutlineManager;

/** 
 * 说明：外线配置
 * 
 * 创建时间：2016-11-13
 */
@Controller
@RequestMapping(value="/outline")
public class OutlineController extends BaseController {
	
	String menuUrl = "outline/list.do"; //菜单地址(权限用)
	@Resource(name="outlineService")
	private OutlineManager outlineService;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"新增Outline");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("OUTLINE_ID", this.get32UUID());	//主键
		CallResult return_result=outlineService.save(pd);
		
		if(return_result.getCode().equals(0)){
			mv.addObject("msg","success");
			mv.setViewName("save_result");
		}else {
			mv.addObject("exception","[ code="+return_result.getCode()+",errmsg="+return_result.getErrmsg()+",result="+return_result.getResult()+" ]");
			mv.setViewName("error");
		}
		return mv;
	}
	
	/**删除
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"删除Outline");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		PageData pd = new PageData();
		pd = this.getPageData();
		outlineService.delete(pd);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"修改Outline");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		outlineService.edit(pd);
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(Page page) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"列表Outline");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		page.setPd(pd);
		List<PageData>	varList = outlineService.list(page,pd);	//列出Outline列表
		mv.setViewName("system/outline/outline_list");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		mv.addObject("QX",Jurisdiction.getHC());	//按钮权限
		return mv;
	}
	
	
	/**去新增页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goAdd")
	public ModelAndView goAdd()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		mv.setViewName("system/outline/outline_edit");
		mv.addObject("msg", "save");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	 /**去修改页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goEdit")
	public ModelAndView goEdit()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd = outlineService.findByUsername(pd);
		mv.setViewName("system/outline/outline_edit");
		mv.addObject("msg", "edit");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	 /**批量删除
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/deleteAll")
	@ResponseBody
	public Object deleteAll() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"批量删除Outline");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		PageData pd = new PageData();		
		Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getPageData();
		List<PageData> pdList = new ArrayList<PageData>();
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			outlineService.deleteAll(ArrayDATA_IDS);
			pd.put("msg", "ok");
		}else{
			pd.put("msg", "no");
		}
		pdList.add(pd);
		map.put("list", pdList);
		return AppUtil.returnObject(pd, map);
	}
	
	 /**导出到excel
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/excel")
	public ModelAndView exportExcel() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"导出Outline到excel");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;}
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("外线号码");	//1
		titles.add("服务器地址");	//2
		titles.add("注册密码");	//3
		titles.add("是否注册");	//4
		titles.add("外线类型");	//5
		titles.add("端口号");	//6
		titles.add("外线呼叫超时时间");	//7
		titles.add("外线呼入类型");	//8
		titles.add("外线呼入IVR");	//9
		titles.add("呼入分机");	//10
		titles.add("呼入队列");	//11
		titles.add("外线显示信息");	//12
		titles.add("外线强制显示信息");	//13
		titles.add("黑名单类型");	//14
		dataMap.put("titles", titles);
		List<PageData> varOList = outlineService.listAll(pd);
		List<PageData> varList = new ArrayList<PageData>();
		for(int i=0;i<varOList.size();i++){
			PageData vpd = new PageData();
			vpd.put("var1", varOList.get(i).getString("USERNAME"));	//1
			vpd.put("var2", varOList.get(i).getString("REALM"));	//2
			vpd.put("var3", varOList.get(i).getString("PASSWORD"));	//3
			vpd.put("var4", varOList.get(i).getString("REGISTER"));	//4
			vpd.put("var5", varOList.get(i).getString("TYPE"));	//5
			vpd.put("var6", varOList.get(i).get("PORT").toString());	//6
			vpd.put("var7", varOList.get(i).get("TIMEOUT").toString());	//7
			vpd.put("var8", varOList.get(i).getString("INCOMING_TYPE"));	//8
			vpd.put("var9", varOList.get(i).getString("INCOMING_IVR"));	//9
			vpd.put("var10", varOList.get(i).get("INCOMING_EXTEN").toString());	//10
			vpd.put("var11", varOList.get(i).getString("INCOMING_CALLCENTER"));	//11
			vpd.put("var12", varOList.get(i).getString("EFFECTIVE_CALLER_ID_NAME"));	//12
			vpd.put("var13", varOList.get(i).getString("FORCE_EFFECTIVE_CALLER_ID_NAME"));	//13
			vpd.put("var14", varOList.get(i).getString("TYPE_BLACKLIST"));	//14
			varList.add(vpd);
		}
		dataMap.put("varList", varList);
		ObjectExcelView erv = new ObjectExcelView();
		mv = new ModelAndView(erv,dataMap);
		return mv;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
