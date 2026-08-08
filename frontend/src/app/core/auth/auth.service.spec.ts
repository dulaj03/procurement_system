import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return null token when not logged in', () => {
    expect(service.getAccessToken()).toBeNull();
  });

  it('should return false for isLoggedIn when no token stored', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should return null for currentUser when not logged in', () => {
    expect(service.currentUser).toBeNull();
  });

  it('should emit null from currentUser$ initially', (done) => {
    service.currentUser$.subscribe(user => {
      expect(user).toBeNull();
      done();
    });
  });

  it('should return false for hasRole when not logged in', () => {
    expect(service.hasRole('ROLE_ADMIN')).toBeFalse();
  });

  it('should detect expired token correctly', () => {
    // Expired JWT (exp in the past)
    const expiredPayload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) - 3600 }));
    const expiredToken = `header.${expiredPayload}.signature`;
    localStorage.setItem('procure_access_token', expiredToken);

    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should detect valid token correctly', () => {
    // Valid JWT (exp in the future)
    const validPayload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
    const validToken = `header.${validPayload}.signature`;
    localStorage.setItem('procure_access_token', validToken);

    expect(service.isLoggedIn()).toBeTrue();
  });
});
