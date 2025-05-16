package com.example.demo.Models;

import java.util.ArrayList;
import java.util.List;

public class ImplementingInterface {
    public static void FindImplementingInterfaces(TypeMeta typeMeta, ClassPropertyMethodCall cpmc) {
        // now traverse starting at type meta interfaces
        List<TypeMeta> toSearch = new ArrayList<TypeMeta>();
        for(TypeMeta implemented : typeMeta.implementedTypes) {
            for(String interfaceMethodName : implemented.methodNames) {
                if (interfaceMethodName.equals(cpmc.methodName)) {
                    boolean alreadyAdded = false;
                    for(TypeMeta existing: cpmc.interfacesThatImplement) {
                        if (existing == implemented) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        cpmc.interfacesThatImplement.add(implemented);
                    }
                }
            }
            for(TypeMeta implementedParent : implemented.extendedTypes) {
                if (implementedParent.isInterface) {
                    toSearch.add(implementedParent);
                }
            }
        }
        for(TypeMeta implemented : toSearch) {
            // refactor this to separate method with above
            for(String interfaceMethodName : implemented.methodNames) {
                if (interfaceMethodName.equals(cpmc.methodName)) {
                    boolean alreadyAdded = false;
                    for(TypeMeta existing: cpmc.interfacesThatImplement) {
                        if (existing == implemented) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        cpmc.interfacesThatImplement.add(implemented);
                    }
                }
            }
        }
        for(TypeMeta extended : typeMeta.extendedTypes) {
            FindImplementingInterfaces(extended, cpmc);
        }
    }
}
