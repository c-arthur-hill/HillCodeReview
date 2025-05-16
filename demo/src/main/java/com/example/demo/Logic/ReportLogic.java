package com.example.demo.Logic;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import com.example.demo.Models.ClassProperty;
import com.example.demo.Models.ClassPropertyMethodCall;
import com.example.demo.Models.ImplementingInterface;
import com.example.demo.Models.TypeMeta;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.eclipse.jgit.api.Git;

public class ReportLogic {
    private List<String> namespaceTypes;
    private List<File> javaFiles = new ArrayList<File>();
    private Map<String, TypeMeta> typeMetas = new HashMap<String, TypeMeta>();
    public String error = "";
    private String destTmpDir;
    final String PACKAGE_DEFAULT = ".";


    public ArrayList<TypeMeta> GetReportTypeMetas(String gitPath) {
        //this.destTmpDir = "C:\\Users\\calvin\\Projects\\DemoTarget";
        CloneGitRepo(gitPath);
        SetDirectoryFiles();
        SetNamespaceTypes();
        SetTypeMetas();
        SetImplementingInterfaces();
        var ret = new ArrayList<TypeMeta>();
        for(var tm : this.typeMetas.entrySet()) {
            ret.add(tm.getValue());
        }
        CleanUp();
        return ret;
    }

    // https://www.geeksforgeeks.org/cloning-a-git-repository-with-jgit/
    private void CloneGitRepo(String gitRepo) {
        String proto = "https://";
        String domain = "github.com";
        if (!gitRepo.contains(domain)) {
            this.error = "Provided repository path is invalid. It doesn't contain 'Github.com'";
            return;
        }
        var startIndex = gitRepo.indexOf(domain);
        var domainLen = domain.length();
        var relPath = gitRepo.substring(startIndex + domainLen);
        String repoUrl = proto + domain + relPath;
        File helper = Paths.get("Tmp").toFile();
        if (!helper.exists()) {
            helper.mkdir();
        }
        File dstFile = Paths.get(helper.getAbsolutePath(), UUID.randomUUID().toString()).toFile();
        this.destTmpDir = dstFile.getAbsolutePath();

        try {
            // https://stackoverflow.com/questions/70668675/jgit-unable-to-delete-git-directory-after-cloning
            Git cloneCmd = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(dstFile)
                .call();
            cloneCmd.close();
        } catch (Exception e) {
            this.error = e.getMessage();
        }
    }  
    
    // https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
    void delete(String fPath) throws IOException {
        File f = new File(fPath);
        if (f.isDirectory()) {
            for (File c : f.listFiles())
            delete(c.getAbsolutePath());
            
        }
        

    }
    

    // https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
    private void CleanUp() {
        try {
            delete(this.destTmpDir); 
        } catch (IOException e) {
            this.error = e.getMessage();
        }
    }

    private void SetDirectoryFiles() {
        try (Stream<Path> stream = Files.walk(Paths.get(this.destTmpDir))) {
            stream.filter(Files::isRegularFile).map((x) -> x.toString()).filter((x) -> x.contains(".java")).forEach((x) -> javaFiles.add(new File(x.toString())));
            
        } catch(IOException e) {
            this.error = e.getMessage();
        }
        
    }

    private void SetNamespaceTypes() {
        // namespace.classname
        // .classname for default
        this.namespaceTypes = new ArrayList<String>();
        // String NAMESPACE+className -> TypeMeta
        if (this.javaFiles != null) {
            // first associate each namespace with a list of types
            for (File file : this.javaFiles) {
                try {
                    CompilationUnit parsed2 = StaticJavaParser.parse(file);
                    var packageDec = getPackageString(parsed2.getPackageDeclaration());
                    for(var type: parsed2.getTypes()) {
                        this.namespaceTypes.add(packageDec + "." + type.getNameAsString());						
                    }
                } catch (Exception e) {
                    var deb = 0;
                }
            }
        }
    } 

    private HashSet<String> getFileFullClassPaths(CompilationUnit parsed2, String packageDec) {
        HashSet<String> fileFullClassPaths = new HashSet<String>();

        for(var imp : parsed2.getImports()) {
            var impName = imp.getNameAsString();
            if (impName.contains(".*")) {
                impName = impName.replace(".*", "");
            }
            for(var packDef : this.namespaceTypes) {
                if (packDef.startsWith(impName)) {
                    fileFullClassPaths.add(packDef);
                }
            }
        }

        for(var defaultImp : this.namespaceTypes) {
            if (defaultImp.startsWith(packageDec)) {
                fileFullClassPaths.add(defaultImp);
            }
        }
        return fileFullClassPaths;
    }

    private TypeMeta addOrGetTypeMeta(String className) {
        TypeMeta typeMeta;
        if (typeMetas.containsKey(className)) {
            typeMeta = this.typeMetas.get(className);
        } else {
            typeMeta = new TypeMeta();
            typeMeta.className = className;
            this.typeMetas.put(className, typeMeta);
        }
        return typeMeta;
    }

    private String getFileFullClassPath(String className, HashSet<String> fileFullClassPaths) {
        for(var cand : fileFullClassPaths) {
            if (cand.endsWith("." + className)) {
                return cand;
            }
        }
        return className;
    }

    private void setExtendedTypes(TypeMeta typeMeta, ClassOrInterfaceDeclaration coid, HashSet<String> fileFullClassPaths) {
        for(var ext : coid.getExtendedTypes()) {
            var extensionName = getFileFullClassPath(ext.getNameAsString(), fileFullClassPaths);
            
            TypeMeta extensionTypeMeta = addOrGetTypeMeta(extensionName);
            typeMeta.extendedTypes.add(extensionTypeMeta);
        }
    }

    private void setImplementedTypes(TypeMeta typeMeta, ClassOrInterfaceDeclaration coid, HashSet<String> fileFullClassPaths) {
        for(var impl : coid.getImplementedTypes()) {
            var implementationName = getFileFullClassPath(impl.getNameAsString(), fileFullClassPaths);
            TypeMeta implementationTypeMeta = addOrGetTypeMeta(implementationName);
            typeMeta.implementedTypes.add(implementationTypeMeta);							
        }
    }

    private void setFields(TypeDeclaration<?> type, TypeMeta typeMeta, HashSet<String> fileFullClassPaths) {
        for(var property : type.getFields()) {
            ClassProperty classProperty = new ClassProperty();
            classProperty.classMeta = typeMeta;
            // when field is initialized to an object this will find the type 
            // when field is declared but not initialized it will also find the type.
            var propertyTypeFound = property.findFirst(ClassOrInterfaceType.class);
            ClassOrInterfaceType tt;
            if (propertyTypeFound.isPresent()) {
                tt = propertyTypeFound.get();
            } else {
                continue;
            }
            if (!tt.isClassOrInterfaceType()) {
                continue;
            }
            var propType = getFileFullClassPath(tt.getNameAsString(), fileFullClassPaths);
            TypeMeta propTypeMeta = addOrGetTypeMeta(propType);
            classProperty.propertyType = propTypeMeta;
            // only one loop here, really like a null check
            for(var potentialPropertyName : property.getVariables()) {
                classProperty.propertyName = potentialPropertyName.getNameAsString();
            }
            if (null == classProperty.propertyType) {
                continue;
            }
            typeMeta.properties.add(classProperty);
        }
    }

    private void setParsedTypeMetas(CompilationUnit parsed2, String packageDec, HashSet<String> fileFullClassPaths) {
        for(var type : parsed2.getTypes()) {
            // this is not packageDec aware
            ClassOrInterfaceDeclaration coid = (ClassOrInterfaceDeclaration)type;
            var classNameInit = packageDec + "." + type.getNameAsString();
            String className = null;
            for(var potentialMatch : fileFullClassPaths) {
                if (potentialMatch.equals(classNameInit)) {
                    className = potentialMatch;
                    break;
                }
            }
            if (className == null) {
                continue;
            }
            TypeMeta typeMeta = addOrGetTypeMeta(className);
            setExtendedTypes(typeMeta, coid, fileFullClassPaths);
            setImplementedTypes(typeMeta, coid, fileFullClassPaths);
            if (coid != null) {
                typeMeta.isInterface = ((ClassOrInterfaceDeclaration)type).isInterface();
            }
            setFields(type, typeMeta, fileFullClassPaths);
            for(var method : type.getMethods()) {
                typeMeta.methodNames.add(method.getNameAsString());
            }
        }
    }

    private String getPackageString(Optional<PackageDeclaration> packageObj)
    {
        String packageDec;
        if (packageObj.isPresent()) {
            packageDec = packageObj.get().getNameAsString();
        } else {
            packageDec = PACKAGE_DEFAULT;
        }
        return packageDec;
    }

    private void SetTypeMetas() {
        for (File file : this.javaFiles) {
            try {
                CompilationUnit parsed2 = StaticJavaParser.parse(file);
                var packageObj = parsed2.getPackageDeclaration();
                String packageDec = getPackageString(packageObj);
                HashSet<String> fileFullClassPaths = getFileFullClassPaths(parsed2, packageDec);
                setParsedTypeMetas(parsed2, packageDec, fileFullClassPaths);
            } catch (Exception e) {
                this.error = e.getMessage();
            }		
        }
    }

    private void SetImplementingInterfaces() {
        for (File file : this.javaFiles) {		
            try {
                CompilationUnit parsed2 = StaticJavaParser.parse(file);
                var packageDec = getPackageString(parsed2.getPackageDeclaration());
                HashSet<String> fileFullClassPaths = getFileFullClassPaths(parsed2, packageDec);
                for(var type : parsed2.getTypes()) {
                    var classNameInit = packageDec + "." + type.getNameAsString();
                    String className = null;
                    for(var potentialMatch : fileFullClassPaths) {
                        if (potentialMatch.equals(classNameInit)) {
                            className = potentialMatch;
                            break;
                        }
                    }
                    if (className == null) {
                        continue;
                    }
                    TypeMeta typeMeta = this.typeMetas.get(className);
                    if (typeMeta == null) {
                        continue;
                    }
                    // then check if an interface implements that same member and is reachable
                    for(var member : type.getMethods()) {
                        for(var stmt : member.findAll(MethodCallExpr.class)) {
                            // need caller variable
                            String fieldName = null;
                            for(var tt : stmt.findAll(NameExpr.class)) {
                                // there should only be one here in the method call expression -- not sure why this isn't just optional
                                fieldName = tt.getNameAsString();
                            }
                            ClassProperty matchesFieldName = null;
                            for(ClassProperty property : typeMeta.properties) {
                                if (property.propertyName.equals(fieldName)) {
                                    matchesFieldName = property;
                                }
                            }
                            if (matchesFieldName == null || matchesFieldName.propertyType.isInterface) {
                                continue; // move to next method call expr
                            }
                            ClassPropertyMethodCall cpmc = new ClassPropertyMethodCall();
                            cpmc.methodName = stmt.getNameAsString();
                            cpmc.classProperty = matchesFieldName;
                            ImplementingInterface.FindImplementingInterfaces(cpmc.classProperty.propertyType, cpmc);
                            matchesFieldName.methodCalls.add(cpmc);
                            // now do same with each parent
                        }
                    }
                }
            } catch (Exception e) {
                this.error = e.getMessage();
            }		
        }
    }
}
