package org.upnext.authservice.utils;

import java.lang.reflect.Field;

public class CopyFieldsToClass {
    public static void copyNonNullAndNonBlankFields(Object source, Object target) {
        System.out.println("Copying non-blank fields from " + source.getClass().getName() + " to " + target.getClass().getName());
        Field[] fileds = source.getClass().getDeclaredFields();
        for (Field field : fileds) {
            field.setAccessible(true);
            // email cannot be updated
            if(field.getName().equals("email")) continue;
            System.out.println(field);
            try{
                Object value = field.get(source);
                if(value != null) {
                    if(value instanceof String) {
                        if(!((String) value).isBlank()){
                            setFieldInTarget(target, field.getName(), value);
                        }
                    }else{
                        setFieldInTarget(target, field.getName(), value);
                    }
                }
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    static void setFieldInTarget(Object target, String fieldName, Object value){
        try{
            Field targetField = target.getClass().getDeclaredField(fieldName);
            targetField.setAccessible(true);
            targetField.set(target, value);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
