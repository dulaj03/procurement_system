import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { authGuard, noAuthGuard } from './auth.guard';
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router
} from '@angular/router';

describe('authGuard', () => {
  let authService: AuthService;
  let router: Router;

  const mockRoute = { data: {} } as unknown as ActivatedRouteSnapshot;
  const mockState = { url: '/dashboard' } as RouterStateSnapshot;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientTestingModule],
      providers: [AuthService]
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should redirect to login when not logged in', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(false);
    const navigateSpy = spyOn(router, 'navigate');

    const result = TestBed.runInInjectionContext(() => authGuard(mockRoute, mockState));

    expect(result).toBeFalse();
    expect(navigateSpy).toHaveBeenCalledWith(
      ['/auth/login'],
      { queryParams: { returnUrl: '/dashboard' } }
    );
  });

  it('should allow access when logged in with no role restriction', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);

    const result = TestBed.runInInjectionContext(() => authGuard(mockRoute, mockState));

    expect(result).toBeTrue();
  });
});

describe('noAuthGuard', () => {
  let authService: AuthService;
  let router: Router;

  const mockRoute = { data: {} } as unknown as ActivatedRouteSnapshot;
  const mockState = { url: '/auth/login' } as RouterStateSnapshot;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientTestingModule],
      providers: [AuthService]
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should allow access to login page when not logged in', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(false);
    const result = TestBed.runInInjectionContext(() => noAuthGuard(mockRoute, mockState));
    expect(result).toBeTrue();
  });

  it('should redirect to dashboard when already logged in', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    const navigateSpy = spyOn(router, 'navigate');

    const result = TestBed.runInInjectionContext(() => noAuthGuard(mockRoute, mockState));

    expect(result).toBeFalse();
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
  });
});
