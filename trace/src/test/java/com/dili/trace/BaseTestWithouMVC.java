package com.dili.trace;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.servlet.ServletContext;

import com.dili.ss.retrofitful.annotation.RestfulScan;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import com.dili.ss.retrofitful.RestfulFactoryBean;
import com.dili.ss.retrofitful.RetrofitfulRegistrar;

import mockit.Invocation;
import mockit.MockUp;
import tk.mybatis.spring.annotation.MapperScan;

//@RunWith(SpringRunner.class)

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@WebAppConfiguration("src/main/resources")
//@TestExecutionListeners(mergeMode = MergeMode.MERGE_WITH_DEFAULTS, inheritListeners = true, value = {
//		MockitoDependencyInjectionTestExecutionListener.class }) // ,
// DependencyInjectionTestExecutionListener.class
// })
@EnableTransactionManagement
//@Transactional
@Transactional(propagation = Propagation.NEVER)
@Rollback
//处理事务支持
@EnableAspectJAutoProxy(proxyTargetClass = true)
@MapperScan(basePackages = {"com.dili.ss.uid.mapper", "com.dili.trace.dao", "com.dili.ss.dao"})
@ComponentScan(basePackages = {"com.dili.ss.uid", "com.dili.ss", "com.dili.trace", "com.dili.common", "com.dili.commons", "com.dili.uap.sdk"})
@RestfulScan({"com.dili.orders.rpc", "com.dili.ss.uid", "com.dili.trace.rpc", "com.dili.uap.sdk.rpc", "com.dili.bpmc.sdk.rpc"})
//@DTOScan({"com.dili.trace","com.dili.ss"})
//@Import(DynamicRoutingDataSourceRegister.class)
@EnableScheduling

@EnableAsync
@EnableFeignClients(basePackages = {"com.dili.orders.rpc", "com.dili.ss.uid", "com.dili.assets.sdk.rpc"
		, "com.dili.customer.sdk.rpc"
		, "com.dili.trace.rpc"
		, "com.dili.bpmc.sdk.rpc"})
@Import(FeignClientsConfiguration.class)
@ServletComponentScan
@EnableDiscoveryClient

public class BaseTestWithouMVC {
	protected static final Logger logger = LoggerFactory.getLogger(BaseTestWithouMVC.class);
	@MockBean
	ErrorAttributes attributes;
	@Autowired
	protected MappingJackson2HttpMessageConverter converter;
	@MockBean
	ServletContext servletContext;

	protected String toJson(Object value) {
		if (value == null) {
			return "";
		} else {

			try {
				return converter.getObjectMapper().writeValueAsString(value);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return String.valueOf(value);
		}
	}

	@BeforeAll
	public static void testBaseBeforeAllInit() {
//		new MockUp<RetrofitfulRegistrar>() {
//			@mockit.Mock
//			public String getClassNameByResource(Resource resource, URL rootDirURL, String basePackage) {
//				JarFile jarFile = null;
//				boolean closeJarFile = false;
//				JarEntry entry;
//				try {
//					URLConnection con = resource.getURL().openConnection();
//					if (con instanceof JarURLConnection) {
//						JarURLConnection entries = (JarURLConnection) con;
//						ResourceUtils.useCachesIfNecessary(entries);
//						jarFile = entries.getJarFile();
////			                String jarFileUrl = entries.getJarFileURL().toExternalForm();
//						entry = entries.getJarEntry();
//						String rootEntryPath = entry != null ? entry.getName() : "";
//						String classFullPath = rootEntryPath.substring(0, rootEntryPath.length() - ".class".length());
//						String classFullName = ClassUtils.convertResourcePathToClassName(classFullPath);
////			                System.out.println("jar包里面的类:"+classFullName);
//						closeJarFile = !entries.getUseCaches();
//						return classFullName;
//					} else {
//						String resourcePath = resource.getURL().getPath();
//						String rootDirPath = this.getRootResource(basePackage, resource).getURI().getPath();
//						String path = resourcePath.substring(resourcePath.lastIndexOf(rootDirPath)
//								+ rootDirPath.length() - basePackage.length() - 1);
//						String classFullPath = path.substring(0, path.length() - ".class".length());
//						return ClassUtils.convertResourcePathToClassName(classFullPath);
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				} finally {
//					if (closeJarFile) {
//						try {
//							if (jarFile != null) {
//								jarFile.close();
//							}
//						} catch (IOException e) {
//						}
//					}
//				}
//				return "";
//			}
//
//			private Resource getRootResource(String basePackage, Resource resource) {
//				PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
//				String basePackagePath = ClassUtils.convertClassNameToResourcePath(basePackage);
//				try {
//					Resource[] resources = resourcePatternResolver.getResources("classpath*:" + basePackagePath + "/");
//
//					if (resources == null) {
//						return null;
//					} else if (resources.length == 1) {
//						return resources[0];
//					} else {
//						return Stream.of(resources).filter(r -> {
//
//							try {
//								return resource.getURL().toString().contains(r.getURI().toString());
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//							return false;
//
//						}).findFirst().orElse(null);
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				return null;
//			}
//
//		};
//		new MockUp<RestfulFactoryBean>() {
//			@mockit.Mock
//			public Object getObject(Invocation invocation) throws Exception {
//				Object real = invocation.proceed();
//				Class<?>[] interfaces = real.getClass().getInterfaces();
//				if (interfaces.length == 1) {
//					Class<?> classToMock = interfaces[0];
//					Object mockedObj = Mockito.mock(classToMock,
//							Mockito.withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(real)));
//
//					return mockedObj;
//				}
//
//				return real;
//			}
//
//		};
		/*new MockUp<SessionContext>() {
			private SessionContext sessionContext = Mockito.mock(SessionContext.class);
			private UserTicket userTicket = Mockito.mock(UserTicket.class);
			{
				Mockito.when(userTicket.getId()).thenReturn(1L);
				Mockito.doReturn(userTicket).when(sessionContext).getUserTicket();
			}

			@mockit.Mock
			public synchronized SessionContext getSessionContext() {
				return sessionContext;
			}

			@mockit.Mock
			public UserTicket getUserTicket() {
				return userTicket;
			}
		};*/
//		class MyMockClass extends MockUp<RestfulFactoryBean> {
//
//			@mockit.Mock
//			public Object foo(Invocation inv) {
//				Object obj = inv.proceed();
//				return obj;
//			}
//		}
//		new MockUp<com.dili.uap.sdk.component.InitListener>() {
//			@mockit.Mock
//			public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
//				System.out.println("==========onApplicationEvent============");
//			}
//
//		};

	}

}
