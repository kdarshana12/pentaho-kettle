/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.repository.pur;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.pur.model.EERoleInfo;
import org.pentaho.di.repository.pur.model.EEUserInfo;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoUser;
import org.pentaho.platform.security.userroledao.ws.UserRoleSecurityInfo;
import org.pentaho.platform.security.userroledao.ws.UserToRoleAssignment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.repository.pur.UserRoleHelper.convertFromProxyPentahoUser;
import static org.pentaho.di.repository.pur.UserRoleHelper.convertFromProxyPentahoUsers;

/**
 * @author Andrey Khayrutdinov
 */
public class UserRoleHelperTest {

  @Test
  public void convertFromProxyPentahoUser_RetunsNull_WhenErrorOccurs() throws Exception {
    IRoleSupportSecurityManager manager = mock( IRoleSupportSecurityManager.class );
    when( manager.constructUser() ).thenThrow( new KettleException() );

    IUser user =
        convertFromProxyPentahoUser( new ProxyPentahoUser(), Collections.<UserToRoleAssignment> emptyList(), manager );
    assertNull( user );
  }

  @Test
  public void convertFromProxyPentahoUser_CopiesDataFromInput() throws Exception {
    IRoleSupportSecurityManager manager = mockSecurityManager( false );

    ProxyPentahoUser pentahoUser = pentahoUser( "name" );
    pentahoUser.setPassword( "password" );
    pentahoUser.setDescription( "desc" );
    pentahoUser.setEnabled( true );

    IUser user = convertFromProxyPentahoUser( pentahoUser, Collections.<UserToRoleAssignment> emptyList(), manager );
    assertNotNull( user );
    assertEquals( pentahoUser.getName(), user.getName() );
    assertEquals( pentahoUser.getName(), user.getLogin() );
    assertEquals( pentahoUser.getPassword(), user.getPassword() );
    assertEquals( pentahoUser.getDescription(), user.getDescription() );
    assertEquals( pentahoUser.getEnabled(), user.isEnabled() );
  }

  @Test
  public void convertFromProxyPentahoUser_CopiesRolesForEeUser() throws Exception {
    IRoleSupportSecurityManager manager = mockSecurityManager( true );

    ProxyPentahoUser pentahoUser = pentahoUser( "name" );

    List<UserToRoleAssignment> assignments = Collections.singletonList( new UserToRoleAssignment( "name", "role" ) );
    EEUserInfo user = (EEUserInfo) convertFromProxyPentahoUser( pentahoUser, assignments, manager );
    assertNotNull( user );
    assertEquals( pentahoUser.getName(), user.getName() );
    assertEquals( 1, user.getRoles().size() );
    assertEquals( "role", user.getRoles().iterator().next().getName() );
  }

  @Test
  public void convertFromProxyPentahoUsers_ReturnsEmptyList_WhenUsersAreAbsent() throws Exception {
    UserRoleSecurityInfo info = new UserRoleSecurityInfo();
    info.setUsers( null );

    IRoleSupportSecurityManager manager = mockSecurityManager( false );

    List<IUser> users = convertFromProxyPentahoUsers( info, manager );
    assertNotNull( users );
    assertTrue( users.isEmpty() );
  }

  @Test
  public void convertFromProxyPentahoUsers_CopiesEachUser() throws Exception {
    UserRoleSecurityInfo info = new UserRoleSecurityInfo();
    info.setUsers( Arrays.asList( pentahoUser( "user1" ), pentahoUser( "user2" ) ) );

    IRoleSupportSecurityManager manager = mockSecurityManager( false );

    List<IUser> users = convertFromProxyPentahoUsers( info, manager );
    assertNotNull( users );
    assertEquals( 2, users.size() );
    assertEquals( "user1", users.get( 0 ).getName() );
    assertEquals( "user2", users.get( 1 ).getName() );
  }

  private static ProxyPentahoUser pentahoUser( String name ) {
    ProxyPentahoUser pentahoUser = new ProxyPentahoUser();
    pentahoUser.setName( name );
    return pentahoUser;
  }

  private static IRoleSupportSecurityManager mockSecurityManager( final boolean eeUsers ) throws KettleException {
    IRoleSupportSecurityManager manager = mock( IRoleSupportSecurityManager.class );
    when( manager.constructUser() ).thenAnswer( new Answer<IUser>() {
      @Override
      public IUser answer( InvocationOnMock invocation ) throws Throwable {
        return eeUsers ? new EEUserInfo() : new UserInfo();
      }
    } );
    when( manager.constructRole() ).thenAnswer( new Answer<IRole>() {
      @Override
      public IRole answer( InvocationOnMock invocation ) throws Throwable {
        return new EERoleInfo();
      }
    } );

    return manager;
  }
}
