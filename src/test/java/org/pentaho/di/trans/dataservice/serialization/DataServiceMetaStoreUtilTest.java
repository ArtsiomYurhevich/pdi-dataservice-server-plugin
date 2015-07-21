/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.dataservice.serialization;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.dataservice.DataServiceMeta;
import org.pentaho.di.trans.dataservice.cache.DataServiceMetaCache;
import org.pentaho.di.trans.dataservice.optimization.PushDownFactory;
import org.pentaho.di.trans.dataservice.optimization.PushDownOptTypeForm;
import org.pentaho.di.trans.dataservice.optimization.PushDownType;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.Collections;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith( MockitoJUnitRunner.class )
public class DataServiceMetaStoreUtilTest {

  public static final String DATA_SERVICE_NAME = "DataServiceNameForLoad";
  public static final String DATA_SERVICE_STEP = "DataServiceStepNameForLoad";
  private TransMeta transMeta;
  private IMetaStore metaStore;
  private DataServiceMeta dataService;

  @Mock
  private DataServiceMetaCache cache;

  private DataServiceMetaStoreUtil metaStoreUtil;
  private MetaStoreFactory<DataServiceMeta> embeddedMetaStore;

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws KettleException, MetaStoreException {
    LogChannel.GENERAL = mock( LogChannelInterface.class );
    doNothing().when( LogChannel.GENERAL ).logBasic( anyString() );

    transMeta = new TransMeta();

    metaStore = new MemoryMetaStore();
    dataService = new DataServiceMeta( transMeta );
    dataService.setName( DATA_SERVICE_NAME );
    dataService.setStepname( DATA_SERVICE_STEP );

    doReturn( null ).when( cache ).get( any( TransMeta.class ), anyString() );

    metaStoreUtil = new DataServiceMetaStoreUtil( Collections.<PushDownFactory>singletonList( new PushDownFactory() {
      @Override public String getName() {
        return "Mock Optimization";
      }

      @Override public Class<? extends PushDownType> getType() {
        return createPushDown().getClass();
      }

      @Override public PushDownType createPushDown() {
        return mock( PushDownType.class );
      }

      @Override public PushDownOptTypeForm createPushDownOptTypeForm() {
        return mock( PushDownOptTypeForm.class );
      }
    } ), cache
    );
  }

  /**
   * Test cases for data service description saving
   *
   * @throws MetaStoreException
   */
  @Test
  public void testToFromTransMeta() throws MetaStoreException {
    metaStoreUtil.save( metaStore, dataService );

    DataServiceMeta dataServiceMeta = metaStoreUtil.getDataServiceByStepName( transMeta, DATA_SERVICE_STEP );
    assertThat( dataServiceMeta, notNullValue() );

    assertEquals( DATA_SERVICE_NAME, dataServiceMeta.getName() );
    assertEquals( DATA_SERVICE_STEP, dataServiceMeta.getStepname() );
  }

}