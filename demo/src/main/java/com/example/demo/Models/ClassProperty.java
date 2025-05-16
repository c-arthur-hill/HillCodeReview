package com.example.demo.Models;

import java.util.*;

public class ClassProperty {
    public TypeMeta classMeta;
    public String propertyName;
    public TypeMeta propertyType;
    //public boolean isInterface;
    public List<ClassPropertyMethodCall> methodCalls = new ArrayList<ClassPropertyMethodCall>();
}
