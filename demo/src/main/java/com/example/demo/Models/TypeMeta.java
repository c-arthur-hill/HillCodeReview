package com.example.demo.Models;
import java.util.*;

public class TypeMeta {
    public boolean isInterface;
    public String className;
    public TypeMeta parent = null;
    public List<TypeMeta> extendedTypes = new ArrayList<TypeMeta>();
    public List<TypeMeta> implementedTypes = new ArrayList<TypeMeta>();
    public List<ClassProperty> properties = new ArrayList<ClassProperty>();
    public List<String> methodNames = new ArrayList<String>();

}
