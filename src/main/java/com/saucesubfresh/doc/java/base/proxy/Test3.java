package com.saucesubfresh.doc.java.base.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 恭喜zhangs报名成功，请凭报名编号2222到现场参加活动
 *
 * 其中pattern中的\\就是转义字符, "."表示匹配任意的字符,+表示出现1次或多次,? 表示出现0次或1次
 *
 * 关键appendReplacement
 *
 * appendReplacement方法：sb是一个StringBuffer，replaceContext待替换的字符串，这个方法会把匹配到的内容替换为replaceContext，并且把从上次替换的位置到这次替换位置之间的字符串也拿到，然后，加上这次替换后的结果一起追加到StringBuffer里（假如这次替换是第一次替换，那就是只追加替换后的字符串啦）。
 *
 * 比如上面的例子：value拿到zhangs后，appendReplacement 会找到匹配模式，会把${name}替换成zhangs，不是把name替换成zhangs。
 *
 *
 *
 * appendTail方法：sb是一个StringBuffer，这个方法是把最后一次匹配到内容之后的字符串追加到StringBuffer中。
 *
 * 比如执行appendTail之前，sb的值是“恭喜zhangs报名成功，请凭报名编号2222”，加来appendTail把剩余的值追加后面。
 * @author: 李俊平
 * @Date: 2021-05-16 12:48
 */
public class Test3 {

  public static void main(String[] args) {
    Map<String, String> data = new HashMap<>();
    data.put("name", "zhangs");
    data.put("code", "2222");
    String content = "恭喜${name}报名成功，请凭报名编号${code}到现场参加活动";
    String pattern = "\\$\\{(.+?)}";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (m.find())
    {
      String key = m.group(1);
      String value = data.get(key);
      m.appendReplacement(sb, value == null ? "" : value);
    }
    m.appendTail(sb);
    System.out.println(sb.toString());
  }
}
