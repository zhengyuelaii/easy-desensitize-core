package io.github.zhengyuelaii.desensitize.core;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * 循环引用脱敏测试
 *
 * <p>
 * 用于验证在对象存在自引用或互相引用的情况下，
 * 脱敏逻辑不会发生无限递归或 StackOverflowError。
 * </p>
 *
 * <p>
 * 这是一个防御性测试，用于锁定框架的安全边界。
 * </p>
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-21
 */
public class CircularReferenceTest {

    @Test
    @DisplayName("对象自引用时不应发生无限递归")
    void should_not_stackoverflow_on_self_reference() {
        Node node = new Node("张老三");
        node.setNext(node); // 自引用

        assertThatCode(() -> EasyDesensitize.mask(node))
                .doesNotThrowAnyException();

        assertThat(node.getName()).isEqualTo("张*三");
        assertThat(node.getNext()).isSameAs(node);
    }

    @Test
    @DisplayName("对象相互引用时不应发生无限递归")
    void should_not_stackoverflow_on_mutual_reference() {
        Node a = new Node("张老三");
        Node b = new Node("李老四");

        a.setNext(b);
        b.setNext(a); // A -> B -> A

        assertThatCode(() -> EasyDesensitize.mask(a))
                .doesNotThrowAnyException();

        assertThat(a.getName()).isEqualTo("张*三");
        assertThat(b.getName()).isEqualTo("李*四");

        assertThat(a.getNext()).isSameAs(b);
        assertThat(b.getNext()).isSameAs(a);
    }

    @Test
    @DisplayName("集合中存在循环引用时应安全处理")
    void should_handle_circular_reference_in_collection() {
        Node a = new Node("张老三");
        Node b = new Node("李老四");

        a.setNext(b);
        b.setNext(a); // A -> B -> A

        List<Node> list = new ArrayList<>();
        list.add(a);
        list.add(b);

        assertThatCode(() -> EasyDesensitize.mask(list))
                .doesNotThrowAnyException();

        assertThat(a.getName()).isEqualTo("张*三");
        assertThat(b.getName()).isEqualTo("李*四");

        assertThat(a.getNext()).isSameAs(b);
        assertThat(b.getNext()).isSameAs(a);
    }

    /* ========= 测试模型 ========= */

    static class Node {

        @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
        private String name;

        private Node next;

        public Node(String name) {
            this.name = name;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public String getName() {
            return name;
        }

        public Node getNext() {
            return next;
        }
    }

}
