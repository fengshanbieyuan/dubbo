/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.common.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.dubbo.common.URL;

/**
 * 在{@link ExtensionLoader}生成Extension的Adaptive Instance时，为{@link ExtensionLoader}提供信息。
 * 
 * @author ding.lid
 * @export
 * 
 * @see ExtensionLoader
 * @see URL
 */

/**
 * GFC:
 * 1、Adaptive可以注解在类或者方法上
 * 2、当Adaptive注解在类上时，Dubbo不会为该类生成代理类（代理类的作用主要是根据url里面的配置来选择合适的拓展类来调用），表示该类就是一个自适应类，逻辑由人工编码完成。仅有两个类有Adaptive注解，分别是AdaptiveCompiler和AdaptiveExtensionFactory
 * 3、当Adaptive注解在方法上时，Dubbo会为该方法生成代理逻辑，相关实现在{@link com.alibaba.dubbo.common.extension.ExtensionLoader#getAdaptiveExtension()}
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {
    
    /**
     * 从{@link URL}的Key名，对应的Value作为要Adapt成的Extension名。
     * <p>
     * 如果{@link URL}这些Key都没有Value，使用 用 缺省的扩展（在接口的{@link SPI}中设定的值）。<br>
     * 比如，<code>String[] {"key1", "key2"}</code>，表示
     * <ol>
     * <li>先在URL上找key1的Value作为要Adapt成的Extension名；
     * <li>key1没有Value，则使用key2的Value作为要Adapt成的Extension名。
     * <li>key2没有Value，使用缺省的扩展。
     * <li>如果没有设定缺省扩展，则方法调用会抛出{@link IllegalStateException}。
     * </ol>
     * <p>
     * 如果不设置则缺省使用Extension接口类名的点分隔小写字串。<br>
     * 即对于Extension接口{@code com.alibaba.dubbo.xxx.YyyInvokerWrapper}的缺省值为<code>String[] {"yyy.invoker.wrapper"}</code>
     * 
     * @see SPI#value()
     */
    /**
     * GFC：value的值是作为一个key，去url中查询该key对应的值，将这个值作为拓展的名字，调用对应的拓展方法，如果不设置这个值，就用@SPI中设置的默认值
     * @return
     */
    String[] value() default {};
    
}