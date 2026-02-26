package benchmark;

import io.github.zhengyuelaii.desensitize.core.EasyDesensitize;
import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.FixedMaskHandler;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import io.github.zhengyuelaii.desensitize.core.util.ClassAnalyzer;
import io.github.zhengyuelaii.desensitize.core.util.Masker;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 列表脱敏性能测试
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-02-13
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS) // 大数据量建议用毫秒
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class ListDesensitizeBenchmark {

    // 通过参数化测试不同的数据量
    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private Map<String, MaskingHandler> handlers;

    private List<User> userList;

    @Setup(Level.Trial)
    public void setup() {
        // 构建测试数据
        userList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            userList.add(getUser(i));
        }
        // 构造解析器
        handlers = new HashMap<>();
        handlers.put("mobile", value -> Masker.hide(value, 3, 7));
        handlers.put("idCard", value -> Masker.hide(value, 1, value.length() - 2));
    }

    private static User getUser(int i) {
        User user = new User();
        user.setId((long) i);
        user.setUsername("user_" + i);
        user.setRealName("测试员" + i);
        user.setMobile("1370000123" + (i % 10));
        user.setEmail(i + "test@example.com");
        user.setIdCard("31011519900101000" + (i % 10));
        user.setAge(i % 100);
        user.setBalance(new BigDecimal("1000.50"));
        user.setCreateTime(new Date());
        user.setVip(i % 2 == 0);

        Address adder = new Address();
        adder.setProvince("上海");
        adder.setCity("上海市");
        adder.setDetail("浦东新区长清路" + i + "号");
        user.setAddress(adder);
        return user;
    }

    @Benchmark
    public void testFrameworkBatch(Blackhole bh) {
        EasyDesensitize.mask(userList, handlers);
        bh.consume(userList);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ListDesensitizeBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    public static class EmailMaskingHandler implements MaskingHandler {
        @Override
        public String getMaskingValue(String value) {
            if (value == null || !value.contains("@")) {
                return value;
            }

            int atIndex = value.indexOf("@");
            // 如果 @ 就在第一位，或者整个字符串只有 @，原样返回或特殊处理
            if (atIndex <= 1) {
                return value;
            }

            // 逻辑：保留第 1 个字符，从第 2 个到 @ 之前全部替换为 *
            String prefix = value.substring(0, 1); // 首字母
            String suffix = value.substring(atIndex); // @ 及其后面的域名部分

            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 1; i < atIndex; i++) {
                sb.append("*");
            }
            sb.append(suffix);

            return sb.toString();
        }
    }

    public static class User {

        private Long id;
        private String username;

        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String realName;
        private String mobile;
        @MaskingField(typeHandler = EmailMaskingHandler.class)
        private String email; // z****@gmail.com
        @MaskingField(typeHandler = FixedMaskHandler.class)
        private String idCard; // ****************

        @MaskingField(typeHandler = FixedMaskHandler.class)
        private String bankCard;

        // --- 普通业务字段 (不脱敏) ---
        private Integer age;
        private BigDecimal balance;
        private Date createTime;
        private boolean vip;

        private Address address;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getBankCard() {
            return bankCard;
        }

        public void setBankCard(String bankCard) {
            this.bankCard = bankCard;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public boolean isVip() {
            return vip;
        }

        public void setVip(boolean vip) {
            this.vip = vip;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", realName='" + realName + '\'' +
                    ", mobile='" + mobile + '\'' +
                    ", email='" + email + '\'' +
                    ", idCard='" + idCard + '\'' +
                    ", bankCard='" + bankCard + '\'' +
                    ", age=" + age +
                    ", balance=" + balance +
                    ", createTime=" + createTime +
                    ", vip=" + vip +
                    ", address=" + address +
                    '}';
        }
    }

    public static class Address {
        private String province;
        private String city;
        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String detail; // 上海市****路

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", detail='" + detail + '\'' +
                    '}';
        }
    }
}
