import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
public class Utils {//клас виводу інформації про вибраний клас, папку, ще якийсь елемент
    //вивід вмісту файлу якщо це не клас (.сlass), та якісь допоміжні файли:
    public static String getLinesOfJar(InputStream input) throws IOException{
        String result="";
        InputStreamReader isr = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(isr);
        String line;
        while ((line = reader.readLine()) != null)
            result+=line+"\n";
        reader.close();
        return result;
    }
    public static String printModifiers(int m) {//перевірка модифікаторів полів
        String result="";
        if (Modifier.isPublic(m)) result+="public ";
        if (Modifier.isPrivate(m)) result+="private ";
        if (Modifier.isProtected(m)) result+="protected ";
        if (Modifier.isVolatile(m)) result+="volatile ";
        if (Modifier.isTransient(m)) result+="transient ";
        if (Modifier.isAbstract(m)) result+="abstract ";
        if(Modifier.isStatic(m))result+="static ";
        if(Modifier.isFinal(m))result+="final ";
        return result;
    }

    public static String getType(String s){
        if(s.contains(".jpg")||s.contains(".gif")||s.contains(".png")||s.contains(".GIF"))return "Изображение";
        if(s.contains(".txt")||s.contains(".xml")||s.contains(".htm")||s.contains(".html"))return "Текст";
        if(s.contains(".wav")||s.contains(".mp3"))return "Звук";
        else return "Неизвестный тип";
    }

}