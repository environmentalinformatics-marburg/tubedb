package tsdb.web.api;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import tsdb.TsDBFactory;
import tsdb.web.generator.Css;
import tsdb.web.generator.Html;
import tsdb.web.generator.Tag;

public class SupplementHandler extends AbstractHandler {
	private static final Logger log = LogManager.getLogger();

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);

		String page = (String) request.getParameter("page");
		if(page==null) {
			page="";
		}
		page = page.replace('.', '_');


		String vis_tsm_path = TsDBFactory.WEBFILES_PATH+"/supplement";
		Path rootDirectory = Paths.get(vis_tsm_path,page);
		log.info(rootDirectory);

		response.setContentType("text/html;charset=utf-8");

		Html html = new Html();

		Css css = html.css;
		css.addLine("body", "background-color:#f6f6f6", "margin:0px");
		css.addLine("table", "background-color:#dddddd");
		css.addLine("div.bg", "background-image:url(../content/timeseries_bg3.png)","background-position:top left","background-size:100%");
		css.addLine("h1", "text-align:center", "margin:0px");
		css.addLine("#top_line", "display:table", "width:100%");
		//#top_line_left {display: table-cell; text-align: left; }
		css.addLine("#top_line_right", "display:table-cell", "text-align:right");
		
		Tag head = html.head;
		head.setAttribute("lang", "en");
		Tag metaRobots = head.addTag("meta");
		metaRobots.setAttribute("name", "robots");
		metaRobots.setAttribute("content", "noindex");
		head.addTag("meta").setAttribute("charset", "utf-8");
		Tag fav = head.addTag("link");
		fav.setAttribute("rel", "icon");
		fav.setAttribute("type", "image/png");
		fav.setAttribute("href", "../content/favicon.png");

		Tag body = html.body;
		Tag divTop = body.addDiv();
		divTop.setClass("bg");
		Tag divTopLine = divTop.addDiv();
		divTopLine.setId("top_line");
		Tag divTopLineRight = divTopLine.addDiv();
		divTopLineRight.setId("top_line_right");
		divTopLineRight.addLink("..", "main-page");
		divTopLineRight.addButton("?").setAttribute("onclick", "var h=document.getElementById('div_help').style;h.display=(h.display=='none')?'inline':'none'");
		Tag h1 = divTop.addTag("h1");
		//h1.addLink("../vis_tsm", "Precomputed Exploratories Visualizations");
		h1.addLink("../supplement", "Supplementary Content");
		

		int lastSlashIndex = page.lastIndexOf("/");
		if(lastSlashIndex>=0) {
			String parent = page.substring(0, lastSlashIndex);
			String current = page.substring(lastSlashIndex);			
			Tag h2 = divTop.addTag("h2");
			h2.addLink("../supplement?page="+parent, parent);
			h2.addText(current);
		} else {			
			divTop.addTag("h2",page);
		}

		try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(rootDirectory);
			ArrayList<Path> directorylist = new ArrayList<Path>();
			ArrayList<Path> fileList = new ArrayList<Path>();

			for(Path subPath:ds) {

				if(Files.isDirectory(subPath)) {
					directorylist.add(subPath);
				} else {
					fileList.add(subPath);
				}


			}

			directorylist.sort(null);


			Tag divDirectories = divTop.addTag("div");			
			for(Path subPath:directorylist) { // directories
				//Tag tr = dtable.addTag("tr");
				String name = subPath.getFileName().toString();
				String subPage = page;
				if(!subPage.isEmpty()) {
					subPage += '/';
				}
				subPage += name;
				divDirectories.addLink("../supplement?page="+subPage).addTag("b", name);
				divDirectories.addText(" . . . ");
			}

			divTop.addTag("hr");
			
			
			Tag divHelp = body.addDiv();
			divHelp.setId("div_help");
			divHelp.setStyle("display:none");
			divHelp.addTag("h2","Help");
			divHelp.addText("On this page you can view additional files.");
			divHelp.addTag("br");
			divHelp.addText("By clicking a link in the area at the top you can navigate to a sub-page or to a parent-page.");
			divHelp.addTag("br");
			divHelp.addText("The area below contains links to files at the current page.");
			divHelp.addTag("hr");

			fileList.sort(null);
			Tag table = body.addTag("table");
			for(Path subPath:fileList) { // files
				Tag tr = table.addTag("tr");
				String name = subPath.getFileName().toString();
				String subPage = page;
				if(!subPage.isEmpty()) {
					subPage += '/';
				}
				subPage += name;
				String fileText = name;
				if(fileText.endsWith(".png")) {
					fileText = fileText.substring(0, fileText.length()-4);
				} else if(fileText.endsWith(".html")) {
					fileText = fileText.substring(0, fileText.length()-5);
				}
				tr.addTag("td").addLink("../files/supplement/"+subPage, fileText);

			}


			
		} catch (Exception e) {
			log.error(e);
		}

		html.write(response.getWriter());
	}

}
