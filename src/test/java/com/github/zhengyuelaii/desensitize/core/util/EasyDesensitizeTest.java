package com.github.zhengyuelaii.desensitize.core.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.zhengyuelaii.desensitize.core.EasyDesensitize;
import com.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

public class EasyDesensitizeTest {
	
	
	@Test
    @DisplayName("Should mask fields in a single Bean using annotations")
    void testMaskBean() {
        UserVO user = new UserVO();
        user.setMobile("13800138000");
        user.setName("张三");

        EasyDesensitize.mask(user);

        // 假设 MobileMaskingHandler 将中间四位变为 *
        assertEquals("138****8000", user.getMobile());
        assertEquals("张三", user.getName()); // 未标记的不应改变
    }

    @Test
    @DisplayName("Should mask nested objects and collections recursively")
    void testRecursiveMasking() {
        UserVO user = new UserVO();
        AddressVO addr = new AddressVO();
        addr.setDetail("北京市朝阳区某街道");
        user.setAddresses(Collections.singletonList(addr));

        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("detail", val -> "MASKED");
        
        EasyDesensitize.mask(user, handlerMap);

        System.out.println(user);
        assertNotEquals("北京市朝阳区某街道", user.getAddresses().get(0).getDetail());
    }

    @Test
    @DisplayName("Should mask Map values based on handlerMap and key matching")
    void testMaskMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("phone", "13800138000");
        data.put("age", 25);

        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        // 这里模拟一个简单的匿名实现
        handlerMap.put("phone", val -> "MASKED");

        EasyDesensitize.mask(data, handlerMap);

        assertEquals("MASKED", data.get("phone"));
        assertEquals(25, data.get("age"));
    }

    @Test
    @DisplayName("Should throw exception when Map key is not a String")
    void testMaskMapInvalidKey() {
        Map<Integer, Object> invalidMap = new HashMap<>();
        invalidMap.put(123, "some data");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EasyDesensitize.mask(invalidMap);
        });

        assertTrue(exception.getMessage().contains("Unsupported Map Key type"));
    }

    @Test
    @DisplayName("Should handle null data gracefully")
    void testNullData() {
        assertDoesNotThrow(() -> EasyDesensitize.mask(null));
    }

    @Test
    @DisplayName("Should mask mixed structures (List of Maps)")
    void testMixedStructures() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("mobile", "13800138000");
        list.add(map);

        Map<String, MaskingHandler> handlerMap = new HashMap<>();
        handlerMap.put("mobile", val -> "MASKED");

        EasyDesensitize.mask(list, handlerMap);

        assertEquals("MASKED", list.get(0).get("mobile"));
    }
	
	
	public class UserVO {

		@MaskingField(typeHandler = MoblieMaskingHandler.class) // 假设已存在此 Handler
		private String mobile;

		private String name;

		private List<AddressVO> addresses;

		public String getMobile() {
			return mobile;
		}

		public void setMobile(String mobile) {
			this.mobile = mobile;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<AddressVO> getAddresses() {
			return addresses;
		}

		public void setAddresses(List<AddressVO> addresses) {
			this.addresses = addresses;
		}

		@Override
		public String toString() {
			return "UserVO [mobile=" + mobile + ", name=" + name + ", addresses=" + addresses + "]";
		}
		
	}

	public class AddressVO {
		
		private String detail;

		public String getDetail() {
			return detail;
		}

		public void setDetail(String detail) {
			this.detail = detail;
		}

		@Override
		public String toString() {
			return "AddressVO [detail=" + detail + "]";
		}

	}

	public static class MoblieMaskingHandler implements MaskingHandler {

		@Override
		public String getMaskingValue(String value) {
			if (value == null)
				return null;
			return Masker.hide(value, 3, 7);
		}

	}

}
