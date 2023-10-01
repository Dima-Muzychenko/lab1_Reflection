import java.util.jar.JarEntry;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")

public class MyNode extends DefaultMutableTreeNode{//допоміжний клас відображення дерева

    private JarEntry jarEntry;

    private String name;

    private Object object;

    private String pathName="/";

    public MyNode(String name,String pathName,JarEntry jarEntry,Object object){

        setUserObject(name);

        this.name=name;

        this.pathName=pathName;

        this.jarEntry=jarEntry;

        this.object=object;

    }

    public Object getObject(){

        return this.object;

    }

    public JarEntry getJarEntry(){

        return this.jarEntry;

    }

    public String getName(){

        return this.name;

    }

    public String getPathName(){

        return this.pathName;

    }

}