package com.example.demo.Logic;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.Models.ClassProperty;
import com.example.demo.Models.ClassPropertyMethodCall;
import com.example.demo.Models.TypeMeta;
import com.example.demo.ViewModels.ReportRow;

public class ViewLogic {
	private List<ReportRow> classRows = new ArrayList<ReportRow>();
	private ArrayList<TypeMeta> typeMetas;
	private ArrayList<ReportRow> filtered = new ArrayList<ReportRow>();
	private int classRowSpan = 0;
	private int methodCallsRowSpan = 0;
	private int propertyRowSpan = 0;

    public List<ReportRow> GetReportView(ArrayList<TypeMeta> typeMetas) {
		this.typeMetas = typeMetas;	
		getTypeMetaRows();
		return filtered;
    }

	private void resetRowSpans()
	{
		classRowSpan = 0;
		methodCallsRowSpan = 0;
		propertyRowSpan = 0;
	}

	private void getTypeMetaRows() {
		for(var tm : this.typeMetas) {
			getTypeMetaPropertyRows(tm);
			
		}
	}

	private void getTypeMetaPropertyRows(TypeMeta tm) {
		resetRowSpans();
		this.classRows = new ArrayList<ReportRow>();
		for(ClassProperty tmcp : tm.properties) {
			propertyRowSpan = 0;
			List<String> replaceAllInterfaces = new ArrayList<String>();
			List<ReportRow> propertyRows = new ArrayList<ReportRow>();
			List<String> propertyInterfaceReplacements = new ArrayList<String>();
			getTypeMetaPropertyMethoCallRows(tm, tmcp, propertyRows, replaceAllInterfaces);
			for(var replaceAllInterface : replaceAllInterfaces){
				boolean supportsAllMethods = true;
				for(var mc : tmcp.methodCalls) {
					boolean interfaceSupportsMethod = false;
					for(var iti: mc.interfacesThatImplement) {
						if (iti.className == replaceAllInterface) {
							interfaceSupportsMethod = true;
							break;
						}
					}
					if(!interfaceSupportsMethod) {
						supportsAllMethods = false;
						break;
					}
				}
				if (supportsAllMethods) {
					propertyInterfaceReplacements.add(replaceAllInterface);
				}
			}

			for(var pr: propertyRows) {
				if (pr.propertyName != null) {
					for(var cleanInterface : propertyInterfaceReplacements) {
						if (!pr.replacementAllInterfaces.contains(cleanInterface)) {
							pr.replacementAllInterfaces.add(cleanInterface);
						}
					}
				}

				pr.propertyRowSpan = propertyRowSpan;
				classRows.add(pr);
			}
			
		}
		if (classRowSpan == 0) {
			++classRowSpan;
		}
		for(var cr: classRows) {
			if (cr.className != null) {
				cr.classRowSpan = classRowSpan;
			}
			this.filtered.add(cr);
		}
	}

	// todo: these should probably be privet var
	private void getTypeMetaPropertyMethoCallRows(
		TypeMeta tm, 
		ClassProperty tmcp, 
		List<ReportRow> propertyRows, 
		List<String> replaceAllInterfaces
		) {
		if (!tmcp.methodCalls.isEmpty()) {
				
			for(var mc :tmcp.methodCalls) {
				methodCallsRowSpan = 0;
				List<ReportRow> methodCallRows = new ArrayList<ReportRow>();
				if (!mc.interfacesThatImplement.isEmpty()) {
					getInterfacesThatImplementRows(tm, mc, tmcp, propertyRows, replaceAllInterfaces, methodCallRows);
					for(var mcr: methodCallRows) {
						mcr.methodRowSpan = methodCallsRowSpan;
						propertyRows.add(mcr);
					}
				} else {
					++propertyRowSpan;
					++classRowSpan;
					++methodCallsRowSpan;
					var reportRow = new ReportRow();
					if (classRowSpan == 1) {
						reportRow.className = tm.className;
					}
					if (propertyRowSpan == 1) {
						reportRow.propertyName = tmcp.propertyName;
					}
					reportRow.methodName = mc.methodName;
					reportRow.calledFromMethodName = mc.calledFromMethodName;
					propertyRows.add(reportRow);
				}
			}
			for(var pr: propertyRows) {
				replaceAllInterfaces.add(pr.replacementInterface);
			}
		
			
		} else {
			
			
			
			//classRows.add(reportRow);
		}
	}

	private void getInterfacesThatImplementRows(
		TypeMeta tm, 
		ClassPropertyMethodCall mc, 
		ClassProperty tmcp, 
		List<ReportRow> propertyRows, 
		List<String> replaceAllInterfaces,
		List<ReportRow> methodCallRows
	) {
		for(var iti : mc.interfacesThatImplement) {
			++propertyRowSpan;
			++classRowSpan;
			++methodCallsRowSpan;
			var reportRow = new ReportRow();
			if (classRowSpan == 1) {
				// only have these on first row
				reportRow.className = tm.className;
			}
			if (propertyRowSpan == 1) {
				reportRow.propertyName = tmcp.propertyName;
			}
			if (methodCallsRowSpan == 1) {
				reportRow.methodName = mc.methodName;
				reportRow.calledFromMethodName = mc.calledFromMethodName;

			}
			if (!replaceAllInterfaces.contains(iti.className)){
				replaceAllInterfaces.add(iti.className);
			}

			reportRow.replacementInterface = iti.className;
			methodCallRows.add(reportRow);
		}
	}
}
