package com.example.demo.Controllers;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.Logic.ReportLogic;
import com.example.demo.Logic.ViewLogic;
import com.example.demo.Models.TypeMeta;


@SpringBootApplication
@Controller
public class DemoApplication {
    public static void main(String[] args) {
      SpringApplication.run(DemoApplication.class, args);
    }
	@GetMapping("/index")
    public String index() {
		return "index";
	}

	@GetMapping("/about")
	public String report() {
		return "about";
	}

	@GetMapping("/report")
	public String getReport(Model model) {
		return "report";
	}
	
    @PostMapping("/report")
    public String report(@RequestParam String gitPath, Model model) {
		if (gitPath == "") {
			gitPath = "https://github.com/c-arthur-hill/DemoTarget.git";
		}
		String e;
		try {
			// change to di
			ReportLogic reportLogic = new ReportLogic();
			ViewLogic viewLogic = new ViewLogic();
			ArrayList<TypeMeta> typeMetas = reportLogic.GetReportTypeMetas(gitPath);
			var reportRows = viewLogic.GetReportView(typeMetas);
			e = reportLogic.error;
			model.addAttribute("reportRows", reportRows);
		} catch (Exception ex) {
			e = ex.getMessage();
		}
		model.addAttribute("errors", e);

		return "report";		
    }

}