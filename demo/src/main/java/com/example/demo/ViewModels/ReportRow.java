package com.example.demo.ViewModels;
import java.util.ArrayList;
import java.util.List;
public class ReportRow {
    public String className;
    public String propertyName;
    public String calledFromMethodName;
    public String methodName;
    public String replacementInterface;
    public List<String> replacementAllInterfaces = new ArrayList<String>();
    public int classRowSpan = 1;
    public int propertyRowSpan = 1;
    public int methodRowSpan = 1;
    
}
