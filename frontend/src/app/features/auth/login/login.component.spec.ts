import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create the login component', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should initialize login form with empty fields', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const emailControl = component.loginForm.get('email');
    const passwordControl = component.loginForm.get('password');

    expect(emailControl?.value).toBe('');
    expect(passwordControl?.value).toBe('');
  });

  it('should mark email as invalid if empty on submit', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.onSubmit();

    const emailControl = component.loginForm.get('email');
    expect(emailControl?.errors?.['required']).toBeTruthy();
  });

  it('should mark email as invalid with wrong format', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.loginForm.get('email')?.setValue('invalid-email');
    component.loginForm.get('email')?.markAsTouched();

    expect(component.loginForm.get('email')?.errors?.['email']).toBeTruthy();
  });

  it('should mark password as required if empty on submit', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.loginForm.get('email')?.setValue('admin@procure.com');
    component.onSubmit();

    const passwordControl = component.loginForm.get('password');
    expect(passwordControl?.errors?.['required']).toBeTruthy();
  });

  it('should have loading=false initially', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.loading).toBeFalse();
  });

  it('should have submitted=false initially', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.submitted).toBeFalse();
  });

  it('should mark form as invalid when both fields empty', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.loginForm.invalid).toBeTrue();
  });

  it('should mark form as valid with correct email and password', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.loginForm.get('email')?.setValue('admin@procure.com');
    component.loginForm.get('password')?.setValue('Admin@12345');

    expect(component.loginForm.valid).toBeTrue();
  });
});
