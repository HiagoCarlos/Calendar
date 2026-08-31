import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ResetPassword } from './reset-password';

describe('ResetPassword', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResetPassword],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(ResetPassword);
    fixture.detectChanges();
    return fixture;
  }

  it('should render the required fields, submit button, and login navigation', () => {
    const fixture = createComponent();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('input[formControlName="password"]')).toBeTruthy();
    expect(element.querySelector('input[formControlName="password_confirmation"]')).toBeTruthy();
    expect(element.querySelector('button[type="submit"]')?.textContent?.trim()).toBe(
      'Resetar senha',
    );
    expect(element.querySelector('a[routerLink="/login"]')).toBeTruthy();
  });

  it('should show required errors after an invalid submit', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance as any;

    component.onSubmit();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('#password-error')?.textContent).toContain(
      'Informe uma nova senha.',
    );
    expect(element.querySelector('#password-confirmation-error')?.textContent).toContain(
      'Confirme a nova senha.',
    );
  });

  it('should require a password with at least eight characters', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance as any;

    component.form.patchValue({ password: '1234567' });
    component.onSubmit();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('#password-error')?.textContent).toContain(
      'A senha deve ter pelo menos 8 caracteres.',
    );
  });

  it('should show an error when passwords do not match', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance as any;

    component.form.setValue({
      password: 'secure-password',
      password_confirmation: 'different-password',
    });
    component.onSubmit();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('#password-confirmation-error')?.textContent).toContain(
      'As senhas não coincidem.',
    );
    expect(component.form.invalid).toBe(true);
  });

  it('should accept matching valid passwords', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance as any;

    component.form.setValue({
      password: 'secure-password',
      password_confirmation: 'secure-password',
    });

    expect(component.form.valid).toBe(true);
  });

  it('should toggle both password fields between password and text', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance as any;
    const element = fixture.nativeElement as HTMLElement;
    const passwordInput = element.querySelector('#password') as HTMLInputElement;
    const confirmationInput = element.querySelector('#password_confirmation') as HTMLInputElement;

    expect(passwordInput.type).toBe('password');
    expect(confirmationInput.type).toBe('password');

    component.togglePasswordVisibility();
    fixture.detectChanges();

    expect(passwordInput.type).toBe('text');
    expect(confirmationInput.type).toBe('text');
  });
});
