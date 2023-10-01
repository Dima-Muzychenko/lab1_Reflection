import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
public class MyClassLoader extends ClassLoader {//клас, для зчитування класів поза нашої програми
    private String jarName;//шлях до jar файлу
    private JarFile jar;
    @SuppressWarnings("rawtypes")
    private Map<String, Class> loaded = new HashMap<String, Class>();
    public MyClassLoader(String jarName) {//конструктор
        super(MyClassLoader.class.getClassLoader());
        this.jarName = jarName;
        try {
            this.jar = new JarFile( jarName );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Class<?> findClass(String name) throws ClassNotFoundException{//пошук вибраного класу
        Class<?> c = loaded.get(name);//об'єкт типу Class зберігає всю інормацію про певний клас
        if (c!=null)return c;//шукаємо наш клас в шляху по замовчуванні
        try{//якщо в шляху за замовчуванням його нема, то шукаємо в інших місцях
            return findSystemClass(name);//шукаємо в системних класах
        }catch(Exception e){
        }
        byte[] b;
        try{
            b = loadClassData(name);//якщо перші 2 варіанти не спрацювали, то викликаємо цю функцію, що шукає клас з таким же розміром і назвою
            c=defineClass(name, b, 0, b.length);//перевіряємо правильність знайденого класу
            loaded.put(name, c);
        }catch(Throwable e){
// throw new ClassNotFoundException(e.getMessage());
            System.err.println("Class "+name+" not found!");
            return null;
        }return c;
    }
    private byte[] loadClassData(String name) throws ClassNotFoundException {//один з методів пошуку класу
        //(шукає клас з таким же розміром і назвою, як і в класі, що нам потрібен)
        String entryName = name.replace('.', '/') + ".class";
        byte buf[]=new byte[0];
        try {
            JarEntry entry = jar.getJarEntry(entryName);
            if (entry==null){
                throw new ClassNotFoundException(name);
            }
            InputStream input = jar.getInputStream( entry );
            int size = new Long(entry.getSize()).intValue();//розмір класа
            buf = new byte[size];
            int count = input.read(buf/*, 0, size*/);
            if (count < size)
                throw new ClassNotFoundException("Error reading class '"+name+"' from :"+jarName);
        } catch (IOException e1) {
            throw new ClassNotFoundException(e1.getMessage());
        }
        return buf;
    }
}