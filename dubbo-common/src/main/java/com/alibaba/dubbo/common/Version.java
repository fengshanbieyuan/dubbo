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
package com.alibaba.dubbo.common;

import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ClassHelper;

/**
 * Version
 * 
 * @author william.liangf
 */
public final class Version {

    private Version() {}

    private static final Logger logger = LoggerFactory.getLogger(Version.class);

    private static final String VERSION = getVersion(Version.class, "2.0.0");

    private static final boolean INTERNAL = hasResource("com/alibaba/dubbo/registry/internal/RemoteRegistry.class");

    private static final boolean COMPATIBLE = hasResource("com/taobao/remoting/impl/ConnectionRequest.class");

    static {
        // 检查是否存在重复的jar包
    	Version.checkDuplicate(Version.class);
	}

    /**
     * 防痴呆设计
     * https://javatar.iteye.com/blog/804187
     * 在梁飞的博客中描述，很多用户在使用dubbo中发生很多配置错误，所以在dubbo中加入了防痴呆设计，就是为了检查一些常见的错误，总结如下：
     *
     * 1、检查重复的jar包
     * 在引用多个版本的相同jar包的时候会出现新版本的A类调用旧版本的B类的情况，而且和jvm的加载顺序有关，所以在第一条先把它防住，就是在每个jar包中挑一个一定会加载的类，加上重复检查
     * 示例：
     * static {
     *     Duplicate.checkDuplicate(Xxx.class);
     * }
     *
     * 2、检查重复的配置文件
     * 用户经常发生配置文件加载错的问题，也就是正确的配置文件没有被加载，加载了错误的，然后排查正确的配置文件就是没法发现问题，因为jvm会加载发现的第一个配置文件，为了解决这个问题，在加载的地方要检查重复
     * 示例：
     * Duplicate.checkDuplicate("xxx.properties");
     *
     * 3、检查所有的可选配置
     * 比如：在配置直连的时候，注册中心是可选的，但是没有配置直连，配置中心是必选的
     *
     * 4、异常信息给出解决方案
     * 排错时最怕一句话的错误。异常最好报错上下文信息，操作者，操作目标，原因等，最好根据异常，给出解决方案。可以把常见的错误都犯一遍看看能不能根据错误信息搞定问题，或者把平时支持时遇到的问题加到异常信息里
     *
     * 5、日志信息包含环境信息
     * 在排错过程中，经常需要一些环境，注册中心，机器，版本等信息，然而有时候根本不知道或者不注意这些信息，所以直接在日志中打印出来，比较方便
     *
     * 6、kill之前先dump
     * 线上环境无法定位问题，做好先备份现场之后再重启
     *
     *
     */

    public static String getVersion(){
    	return VERSION;
    }
    
    public static boolean isInternalVersion() {
        return INTERNAL;
    }

    public static boolean isCompatibleVersion() {
        return COMPATIBLE;
    }
    
    private static boolean hasResource(String path) {
        try {
            return Version.class.getClassLoader().getResource(path) != null;
        } catch (Throwable t) {
            return false;
        }
    }
    
    public static String getVersion(Class<?> cls, String defaultVersion) {
        try {
            // 首先查找MANIFEST.MF规范中的版本号
            String version = cls.getPackage().getImplementationVersion();
            if (version == null || version.length() == 0) {
                version = cls.getPackage().getSpecificationVersion();
            }
            if (version == null || version.length() == 0) {
                // 如果规范中没有版本号，基于jar包名获取版本号
                CodeSource codeSource = cls.getProtectionDomain().getCodeSource();
                if(codeSource == null) {
                    logger.info("No codeSource for class " + cls.getName() + " when getVersion, use default version " + defaultVersion);
                }
                else {
                    String file = codeSource.getLocation().getFile();
                    if (file != null && file.length() > 0 && file.endsWith(".jar")) {
                        file = file.substring(0, file.length() - 4);
                        int i = file.lastIndexOf('/');
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        }
                        i = file.indexOf("-");
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        }
                        while (file.length() > 0 && ! Character.isDigit(file.charAt(0))) {
                            i = file.indexOf("-");
                            if (i >= 0) {
                                file = file.substring(i + 1);
                            } else {
                                break;
                            }
                        }
                        version = file;
                    }
                }
            }
            // 返回版本号，如果为空返回缺省版本号
            return version == null || version.length() == 0 ? defaultVersion : version;
        } catch (Throwable e) { // 防御性容错
            // 忽略异常，返回缺省版本号
            logger.error("return default version, ignore exception " + e.getMessage(), e);
            return defaultVersion;
        }
    }

    public static void checkDuplicate(Class<?> cls, boolean failOnError) {
        checkDuplicate(cls.getName().replace('.', '/') + ".class", failOnError);
    }

	public static void checkDuplicate(Class<?> cls) {
		checkDuplicate(cls, false);
	}

	public static void checkDuplicate(String path, boolean failOnError) {
		try {
			// 在ClassPath搜文件
			Enumeration<URL> urls = ClassHelper.getCallerClassLoader(Version.class).getResources(path);
			Set<String> files = new HashSet<String>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (url != null) {
					String file = url.getFile();
					if (file != null && file.length() > 0) {
						files.add(file);
					}
				}
			}
			// 如果有多个，就表示重复
			if (files.size() > 1) {
                String error = "Duplicate class " + path + " in " + files.size() + " jar " + files;
                if (failOnError) {
                    throw new IllegalStateException(error);
                } else {
				    logger.error(error);
                }
			}
		} catch (Throwable e) { // 防御性容错
			logger.error(e.getMessage(), e);
		}
	}

}