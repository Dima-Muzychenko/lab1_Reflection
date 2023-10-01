import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MainGUI extends JFrame {
    private JButton openButton;
    private JPanel panelMain;
    private JList<String> listMethods;
    private JList<String> listFields;
    private JList<String> listConstructors;
    DefaultListModel<String> modelMethods, modelFields, modelConstructors;
    File selectedFile = null;
    JTree tree;
    private JTextPane textPane;
    private JScrollPane scrollPane;
    JFrame frame = new JFrame("Lab1");
    JarFile jarFile;

    MainGUI() {
        super("Lab1");
        this.setContentPane(this.panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        modelMethods = new DefaultListModel<String>();
        modelFields = new DefaultListModel<String>();
        modelConstructors = new DefaultListModel<String>();
        listMethods.setModel(modelMethods);//поміщаємо сюди модель, в яку запишемо потрібну нам інформацію
        listFields.setModel(modelFields);
        listConstructors.setModel(modelConstructors);
        openButton.addActionListener(new ActionListener() {//натискання на кнопку вибіру файлу
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(//створюємо навий фільтр пошуку файлів
                        "JAR файлы", "jar");
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Выберите JAR для анализа");
                int returnVal = chooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {//якщо вибрали файл
                    selectedFile = chooser.getSelectedFile();
                    if (!selectedFile.getName().contains(".jar")) {//якщо файл не jar
                        JOptionPane.showMessageDialog(frame, "Это не Jar файл!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    tree = new JTree(new MyNode(selectedFile.getAbsolutePath(), "xz", null, null));
                    scrollPane.setViewportView(tree);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                xzclass();//зображення дерева в інтерфейсі
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    tree.addTreeSelectionListener(treeSelectionListener());//ГОЛОВНЕ
                    ;//функція виводу інформації при натисканні на елемент дерева
                    tree.expandPath(new TreePath(tree.getModel().getRoot()));
                } else {
                    JOptionPane.showMessageDialog(frame, "Не выбран файл!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        MainGUI window = new MainGUI();
        window.setVisible(true);
        //window.frame.setSize(100,200);
        //window.frame.setVisible(true);
    }

    public TreeSelectionListener treeSelectionListener() {//вивід інформації при натисканні
        TreeSelectionListener listener = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        MyNode tr = (MyNode) tree.getLastSelectedPathComponent();//tr-елемент, що вибрали
                        clearAllViews();//стираємо інформації про старі методи, поля...
                        if (tr.getObject() != null) {//якщо там є об'єкти, тобто, якщо це клас (.сlass), а не, наприклад, фото проекту, чи якісь допоміжні файли
                            Class<?> c = (Class<?>) tr.getObject();
                            if (c != null) {
                                Method[] methods = c.getDeclaredMethods();//МЕТОДИ класу
                                for (Method m : methods) {//вивід модифікаторів (printModifiers)
//                                    System.out.println(Utils.printModifiers(m.getModifiers())+m.getReturnType()+" "+m.getName());
                                    modelMethods.addElement(Utils.printModifiers(m.getModifiers()) + m.getReturnType() + " " + m.getName());
                                    ;
                                }
                                Field[] fields = c.getDeclaredFields();//ПОЛЯ класу
                                for (Field f : fields) {
                                    modelFields.addElement(Utils.printModifiers(f.getModifiers()) + f.getType() + " " + f.getName());
                                }
                                Constructor<?>[] constructors = c.getDeclaredConstructors();//КОНСТРУКТОРИ класу
                                for (Constructor<?> cons : constructors) {
                                    modelConstructors.addElement(Utils.printModifiers(cons.getModifiers()) + " " + cons.getName());
                                }
                            }
                        } else {//якщо це не клас (.сlass), а, наприклад, фото проекту, чи якісь допоміжні файли
                            JarEntry j = tr.getJarEntry();//перевіряємо, що вибраний об'єкт належить до нашого jar файлу
                            if (j == null) return;
                            //всю інформацію записуємо в info і потім виводим (textPane.setText(info))
                            String info = j.getName() + "\nРазмер " + j.getSize() + " байт\nДата модификации " + j.getLastModifiedTime();
                            String type = Utils.getType(j.getName());
                            info += "\nТип файла: " + type;
                            if (type.equals("Текст")) {
                                try {
                                    InputStream inputStream = jarFile.getInputStream(j);
                                    info += "\n\nСодержимое файла:\n" + Utils.getLinesOfJar(inputStream);//вивід вмісту файлу
                                } catch (IOException ioException) {
                                    info += "\n\nError read " + j.getName();
                                }
                            }
                            textPane.setText(info);
                        }
                    }
                }).start();
                ;
            }
        };
        return listener;
    }

    public void addNewItem(String name, JarEntry current, Object object) {//графічне відображення дерева
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        MyNode sel = null;
        String[] data = name.split("/");
        if (data.length > 1)
            sel = findUserObject(data[data.length - 2], data);//знаходимо де наш елемент (клас, папка, фото) знаходиться
        MyNode tmp = new MyNode(data[data.length - 1], name, current, object);
        if (sel == null)
            sel = (MyNode) tree.getModel().getRoot();
        model.insertNodeInto(tmp, sel, sel.getChildCount());//графічно відображаємо дерево
        tree.expandPath(new TreePath(model.getRoot()));
    }

    private MyNode findUserObject(Object obj, String[] arr) {//повертає шлях до нашого елементу (класу, папки...)
        ArrayList<MyNode> findList = new ArrayList<>();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        MyNode rootNode = (MyNode) model.getRoot();
        Enumeration<?> enumeration = rootNode.breadthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            MyNode node = (MyNode) enumeration.nextElement();
            if (node.getPathName().contains(obj.toString())) {
                findList.add(node);
            }
        }
        for (MyNode xz : findList) {
            String[] splits = xz.getPathName().split("/");
            if (splits[0].equals(arr[0])) {
                return xz;
            }
        }
        return null;
    }

    public void xzclass() throws IOException {//зображення дерева в інтерфейсі
//        System.out.println(selectedFile.getAbsolutePath());
        ClassLoader loader = new MyClassLoader(selectedFile.getAbsolutePath());
        jarFile = new JarFile(selectedFile.getAbsolutePath());
        Enumeration<JarEntry> enumiration = jarFile.entries();
        String s;
        Class<?> c = null;
        JarEntry e;
//        addNewItem("META-INF",null,null);
//        Object d = new String("text check");
//        addNewItem("test.txt", null, d);

        while (enumiration.hasMoreElements()) {//проход по всім папкам\класам нашого файлу
            e = enumiration.nextElement();
            if (e.isDirectory()) {
                addNewItem(e.getName(), e, null);
            }//якщо папка. Викликаємо функцію відображення
            if (e.getName().contains(".class")) {//якщо це клас
                s = e.getName().replaceAll("/", ".");
                s = s.substring(0, s.length() - 6);
                boolean isErr = false;
                try {
                    c = Class.forName(s, false, loader);
                } catch (ClassNotFoundException e2) {
                    System.out.println("Error:" + e2.getMessage());
                    isErr = true;
                }
                if (!isErr && c != null)//якщо клас
                    addNewItem(e.getName(), e, c);//Викликаємо функцію відображення

            } else {
                if (!e.isDirectory())//якщо не папка і не клас
                    addNewItem(e.getName(), e, null);
            }
        }//jarFile.close();
    }

    public void clearAllViews() {
        modelMethods.clear();
        modelFields.clear();
        modelConstructors.clear();
        textPane.setText("");
    }
}
